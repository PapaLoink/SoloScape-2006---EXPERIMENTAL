# Wilderness bot loot management — feature specification

## Goals
Keep bot-versus-bot deaths rewarding and visible to nearby real players without filling the Wilderness with ground-item objects.

## Death-drop rules
- **Player kills bot:** preserve the existing normal loot rules. Do not apply the bot-vs-bot restrictions below.
- **Bot kills bot:** calculate the defeated bot's three highest-valued carried items (inventory **and** equipment) using the game's normal item valuation rules. **Exclude these three items from the ground-drop set**; they must never appear in the spawned loot for that bot-vs-bot death. Resolve ties deterministically. Retention on bot respawn, if any, should follow the game's existing death/item-retention mechanism.
- For bot-vs-bot deaths near a real player (initial proposal: within 15–20 tiles), allow the *remaining* eligible drops to appear.
- For bot-vs-bot deaths with no real players nearby, do not create physical ground items. Optionally record limited virtual loot for future discovery, if implemented separately.

## Performance safeguards (initial proposed defaults)
- Despawn bot-vs-bot ground drops after **90 seconds**.
- Limit bot-generated loot piles to around **10 per region**; prioritize keeping valuable eligible drops over junk if the cap is reached.
- Keep cleanup separate from normal player-generated loot.

## Acceptance criteria
1. When bot A kills bot B, B's three most valuable eligible carried items **never** appear on the ground, even when a real player is watching.
2. Other eligible bot-vs-bot drops may appear if a real player is nearby, subject to proximity, lifespan, and regional caps.
3. Bot-vs-bot fighting without players nearby does not generate unbounded ground-item piles.
4. When a real player kills a bot, existing full death-drop behavior remains unchanged by this feature.
5. Item selection and drop filtering happen **before** spawning ground-item entities, so protected items are not briefly visible and then removed.

_Status: specification only; implementation not yet complete._
