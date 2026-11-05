"""Batch isolated Castle Wars AI regressions; these are not full combat matches."""

import argparse
import concurrent.futures
import csv
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import time


SERVER = Path(__file__).resolve().parents[1]
MIXED = "com.rs2.bot.CastleWarsMixedLoadChecks"
CAPTURE = "com.rs2.bot.CastleWarsFlagCaptureChecks"
SCOPE = ("Isolated AI/movement/catapult regression plus unopposed capture routes. "
         "No full contested-combat resolution or live-client validation; "
         "results do not establish match scores, win rates, or 0-0 frequency.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--runs", type=int, default=100)
    parser.add_argument("--bots-per-team", type=int, default=20)
    parser.add_argument("--ticks", type=int, default=2000)
    parser.add_argument("--parallel", type=int, default=2)
    parser.add_argument("--first-seed", type=int, default=1)
    parser.add_argument("--timeout", type=int, default=180)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    if min(args.runs, args.parallel, args.timeout) < 1 or args.bots_per_team < 5 or args.ticks < 500:
        parser.error("Use positive runs/parallel/timeout, at least 5 bots per team and 500 ticks.")

    stamp = datetime.now(timezone.utc).strftime("%Y%m%d-%H%M%S-UTC")
    output = (args.output or SERVER / "qa-output" / ("castle-wars-batch-" + stamp)).resolve()
    output.mkdir(parents=True, exist_ok=True)
    classes = output / "classes"
    classes.mkdir(exist_ok=True)
    jar = SERVER / "dist" / "server.jar"
    dependency_cp = str(jar) + os.pathsep + str(SERVER / "lib" / "*")
    sources = [SERVER / "src/test/java" / (name.replace(".", "/") + ".java")
               for name in (MIXED, CAPTURE)]
    compiled = subprocess.run(["javac", "-cp", dependency_cp, "-d", str(classes),
                               *map(str, sources)], cwd=SERVER, capture_output=True, text=True)
    (output / "compile.log").write_text(compiled.stdout + compiled.stderr, encoding="utf-8")
    if compiled.returncode:
        print(compiled.stderr, flush=True)
        return 1
    cp = str(classes) + os.pathsep + dependency_cp
    config = vars(args).copy()
    config["output"] = str(output)
    report = {"scope": SCOPE, "config": config,
              "server_sha256": hashlib.sha256(jar.read_bytes()).hexdigest(), "results": []}
    (output / "README.txt").write_text(SCOPE + "\n", encoding="utf-8")
    print(SCOPE, flush=True)
    print("Report directory: " + str(output), flush=True)
    started = time.monotonic()

    def trial(number):
        seed = args.first_seed + number - 1
        result = {"run": number, "seed": seed, "status": "PASS", "phases": []}
        for name, label in ((MIXED, "mixed"), (CAPTURE, "capture")):
            command = ["java", "-Xmx768m", "-Dprs.test.castleWarsSeed=" + str(seed),
                       "-Dprs.test.castleWarsBotsPerTeam=" + str(args.bots_per_team),
                       "-Dprs.test.castleWarsTicks=" + str(args.ticks), "-cp", cp, name]
            phase_started = time.monotonic()
            log = output / (f"run-{number:03d}-{label}.log")
            try:
                with log.open("w", encoding="utf-8") as stream:
                    process = subprocess.run(command, cwd=SERVER, stdout=stream,
                                             stderr=subprocess.STDOUT, timeout=args.timeout)
                phase = {"name": label, "exit_code": process.returncode,
                         "status": "PASS" if process.returncode == 0 else "FAIL"}
            except subprocess.TimeoutExpired:
                phase = {"name": label, "status": "TIMEOUT"}
            phase["seconds"] = round(time.monotonic() - phase_started, 3)
            phase["log"] = str(log)
            text = log.read_text(encoding="utf-8", errors="replace")
            phase["metrics"] = [line for line in text.splitlines()
                                if line.startswith(("CASTLE_WARS_LOAD_METRICS", "CASTLE_WARS_CATAPULT"))]
            if label == "capture":
                phase["captures"] = text.count("Capture passed:")
            if phase["status"] != "PASS":
                result["status"] = "FAIL"
                phase["failure_tail"] = text[-3000:]
            result["phases"].append(phase)
        return result

    with concurrent.futures.ThreadPoolExecutor(max_workers=args.parallel) as pool:
        futures = [pool.submit(trial, number) for number in range(1, args.runs + 1)]
        for future in concurrent.futures.as_completed(futures):
            result = future.result()
            report["results"].append(result)
            report["results"].sort(key=lambda item: item["run"])
            report["completed"] = len(report["results"])
            report["passed"] = sum(item["status"] == "PASS" for item in report["results"])
            report["failed"] = report["completed"] - report["passed"]
            report["seconds"] = round(time.monotonic() - started, 3)
            (output / "summary.json").write_text(json.dumps(report, indent=2), encoding="utf-8")
            if report["completed"] == 1 or report["completed"] % 10 == 0 or result["status"] != "PASS":
                print(f'{report["completed"]}/{args.runs}: {report["passed"]} passed, '
                      f'{report["failed"]} failed; {report["seconds"]:.1f}s', flush=True)
                if result["status"] != "PASS":
                    print(f'Failed run {result["run"]}, seed {result["seed"]}', flush=True)

    with (output / "summary.csv").open("w", newline="", encoding="utf-8") as stream:
        writer = csv.writer(stream)
        writer.writerow(["run", "seed", "status", "mixed_seconds", "capture_seconds", "captures"])
        for result in report["results"]:
            mixed, capture = result["phases"]
            writer.writerow([result["run"], result["seed"], result["status"],
                             mixed["seconds"], capture["seconds"], capture.get("captures", 0)])
    print("Finished. Summary: " + str(output / "summary.json"), flush=True)
    return 1 if report["failed"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
