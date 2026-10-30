"""Strict compiler for dialogue-only preservation scripts. Rejects unknown gameplay."""
from pathlib import Path
import re,json,csv,textwrap,argparse,subprocess
cli=argparse.ArgumentParser(description=__doc__)
cli.add_argument('--reference',type=Path,default=Path('qa-output/dialogue-reference'))
cli.add_argument('--output',type=Path,default=Path('Server/data/content/npcDialogues2006.tsv'))
cli.add_argument('--audit',type=Path,default=Path('qa-output/npc-dialogue-audit.tsv'))
cli.add_argument('--rejected',type=Path,default=Path('qa-output/reference-rejected.tsv'))
args=cli.parse_args()
root=args.reference
PIN='4949e619a53f40c0fe83395df89bd35ff1f044f4'
commit=subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip()
if commit!=PIN:raise SystemExit('Reference commit differs from audited May 2006 source: '+commit)
pack={v:int(k) for k,v in (s.split('=',1) for s in (root/'pack/npc.pack').read_text().splitlines() if '=' in s)}
class Unsupported(Exception): pass
token=re.compile(r'\s+|//[^\n]*|/\*.*?\*/|"(?:[^"\\]|\\.)*"|[A-Za-z_][\w:]*|\d+|[^\s]',re.S)
class Parser:
 def __init__(self,s): self.t=[m.group() for m in token.finditer(s) if not m.group().isspace() and not m.group().startswith(('//','/*'))];self.i=0
 def pop(self):
  if self.i>=len(self.t): raise Unsupported('unexpected end')
  x=self.t[self.i]; self.i+=1;return x
 def expect(self,x):
  y=self.pop()
  if x!=y:raise Unsupported('expected '+x+', got '+y)
 def peek(self):return self.t[self.i] if self.i<len(self.t) else ''
 def args(self):
  self.expect('(');out=[]
  while self.peek()!=')':
   out.append(self.pop())
   if self.peek()!=')':self.expect(',')
  self.expect(')');self.expect(';');return out
 def stmts(self,block=False):
  out=[]
  while self.peek() and self.peek() not in ('}', 'case', 'default'):
   x=self.pop()
   if x=='~':
    name=self.pop();args=self.args()
    if name in ('chatnpc','chatplayer','mesbox') and len(args)==1 and args[0].startswith('"'):
     out.append(('chat',name,json.loads(args[0])))
    else:raise Unsupported('call '+name)
   elif x=='@':
    name=self.pop()
    if name.startswith('multi') and name[-1:].isdigit():
     args=self.args();n=int(name[-1])
     if len(args)!=2*n:raise Unsupported('dynamic multi')
     pairs=[]
     for i in range(0,len(args),2):
      if not args[i].startswith('"') or not re.fullmatch(r'\w+',args[i+1]):raise Unsupported('dynamic multi')
      pairs.append((json.loads(args[i]),args[i+1]))
     out.append(('multi',pairs))
    else:self.expect(';');out.append(('goto',name))
   elif x=='return':self.expect(';');out.append(('end',))
   elif x in ('def_int','$'):
    if x=='def_int':self.expect('$')
    var=self.pop();self.expect('=')
    if self.peek()=='random':
     self.pop();args=self.args()
     if len(args)!=1 or not args[0].isdigit():raise Unsupported('dynamic random')
     out.append(('random',var,int(args[0])));continue
    self.expect('~');name=self.pop();args=self.args()
    if not re.fullmatch('p_choice[2-5]',name):raise Unsupported('variable '+name)
    n=int(name[-1]);pairs=[]
    if len(args)!=n*2:raise Unsupported('dynamic choice')
    for i in range(0,len(args),2):
     if not args[i].startswith('"') or not args[i+1].isdigit():raise Unsupported('dynamic choice')
     pairs.append((json.loads(args[i]),int(args[i+1])))
    out.append(('choice',var,pairs))
   elif x=='switch_int':
    self.expect('(')
    if self.peek()=='$':
     self.pop();expr=('var',self.pop())
    elif self.peek()=='~':
     self.pop();name=self.pop();self.expect('(');args=[]
     while self.peek()!=')':
      args.append(self.pop())
      if self.peek()!=')':self.expect(',')
     self.expect(')')
     if not re.fullmatch('p_choice[2-5]',name) or len(args)!=int(name[-1])*2:raise Unsupported('switch call '+name)
     expr=('choice',[(json.loads(args[i]),int(args[i+1])) for i in range(0,len(args),2)])
    else:raise Unsupported('switch expression '+self.peek())
    self.expect(')');self.expect('{');cases={};default=[]
    while self.peek()!='}':
     case=self.pop()
     if case=='default':self.expect(':');default=self.stmts();continue
     if case!='case':raise Unsupported('switch case '+case)
     values=[]
     while self.peek()!=':':
      v=self.pop()
      if not v.isdigit():raise Unsupported('dynamic case '+v)
      values.append(int(v))
      if self.peek()!=':':self.expect(',')
     self.expect(':');body=self.stmts()
     for v in values:cases[v]=body
    self.expect('}');out.append(('switch',expr,cases,default))
   elif x=='if':
    self.expect('(');self.expect('$');var=self.pop();self.expect('=');v=self.pop();self.expect(')');self.expect('{')
    if not v.isdigit():raise Unsupported('dynamic condition')
    yes=self.stmts(True);self.expect('}');no=[]
    if self.peek()=='else':
     self.pop()
     if self.peek()=='if':no=self.one_if()
     else:self.expect('{');no=self.stmts(True);self.expect('}')
    out.append(('if',var,int(v),yes,no))
   else:raise Unsupported('statement '+x)
  return out
 def one_if(self):
  # Bound the nested else-if to one statement, preserving following statements.
  self.expect('if');self.expect('(');self.expect('$');var=self.pop();self.expect('=');v=self.pop();self.expect(')');self.expect('{')
  if not v.isdigit():raise Unsupported('dynamic condition')
  yes=self.stmts(True);self.expect('}');no=[]
  if self.peek()=='else':
   self.pop()
   if self.peek()=='if':no=self.one_if()
   else:self.expect('{');no=self.stmts(True);self.expect('}')
  return [('if',var,int(v),yes,no)]

def compile_file(p):
 txt=p.read_text(encoding='utf8');sections={};starts=[]
 matches=list(re.finditer(r'^\[(\w+),([^\]\n]+)\]',txt,re.M))
 for i,m in enumerate(matches):
  typ,name=m.groups();body=txt[m.end():matches[i+1].start() if i+1<len(matches) else len(txt)]
  if typ in ('opnpc1','label'):
   # Two explicit fragments complement stateful quests in this server. Their
   # eligibility is checked by HistoricalNpcDialogues.start, never inferred.
   if typ=='opnpc1' and name=='cook':
    body=body.split('} else if(%cookquest = ^cook_complete) {',1)[1].rsplit('}',1)[0]
   if typ=='opnpc1' and name=='head_wizard':
    greeting=re.search(r'~chatnpc\([^\n]+',body).group()
    default=re.search(r'if\(%runemysteries = \^runemysteries_not_started\) \{(.*?)\}',body,re.S).group(1)
    body=greeting+'\n'+default
   sections[name]=body
   if typ=='opnpc1' and name in pack: starts.append(name)
 nodes={0:('END',[],[])};counter=[1];labels={};active=set();cache={}
 def new(kind,lines,nexts):
  n=counter[0];counter[0]+=1;nodes[n]=(kind,lines,nexts);return n
 def label(name):
  if name in labels:return labels[name]
  if name not in sections:raise Unsupported('external label '+name)
  # Reserve the label before compiling to preserve loops back to menus.
  n=new('JUMP',[],[]);labels[name]=n
  try:nxt=build(Parser(sections[name]).stmts(),{},0)
  except Unsupported:
   labels.pop(name,None);raise
  nodes[n]=('JUMP',[],[nxt]);return n
 def build(stmts,vars,end):
  if not stmts:return end
  s,*rest=stmts;kind=s[0]
  if kind=='chat':
   # Preserve explicit line breaks and wrap when rendering, not during import.
   text=re.sub(r'<p,[^>]+>','',s[2])
   if '<' in text or '>' in text or '\t' in text or '\n' in text:raise Unsupported('dynamic text')
   lines=[]
   for explicit in text.split('|'):lines.extend(textwrap.wrap(explicit,48,break_long_words=False,break_on_hyphens=False) or [''])
   nxt=build(rest,vars,end)
   for offset in reversed(range(0,len(lines),4)):
    nxt=new({'chatnpc':'NPC','chatplayer':'PLAYER','mesbox':'STATEMENT'}[s[1]], lines[offset:offset+4], [nxt])
   return nxt
  if kind=='end':return 0
  if kind=='goto':return label(s[1])
  if kind=='multi':return new('OPTIONS',[q for q,l in s[1]],[label(l) for q,l in s[1]])
  if kind=='choice':return new('OPTIONS',[q for q,v in s[2]],[build(rest,{**vars,s[1]:v},end) for q,v in s[2]])
  if kind=='random':return new('RANDOM',[],[build(rest,{**vars,s[1]:v},end) for v in range(s[2])])
  if kind=='switch':
   if s[1][0]=='choice':return new('OPTIONS',[q for q,v in s[1][1]],[build(s[2].get(v,s[3])+rest,vars,end) for q,v in s[1][1]])
   if s[1][1] not in vars:raise Unsupported('unset switch '+s[1][1])
   return build(s[2].get(vars[s[1][1]],s[3])+rest,vars,end)
  if kind=='if':
   if s[1] not in vars:raise Unsupported('unset condition '+s[1])
   return build((s[3] if vars[s[1]]==s[2] else s[4])+rest,vars,end)
  raise Unsupported(kind)
 result=[]
 for name in starts:
  saved_nodes=nodes.copy();saved_labels=labels.copy();saved_counter=counter[0]
  try:
   start=label(name)
   reachable=set()
   def visit(n):
    if n in reachable:return
    reachable.add(n)
    for v in nodes[n][2]:visit(v)
   visit(start)
   if any(nodes[n][0] in ('NPC','PLAYER','STATEMENT') for n in reachable):result.append((pack[name],start,{n:nodes[n] for n in sorted(reachable)}))
  except Unsupported as e:
   nodes.clear();nodes.update(saved_nodes);labels.clear();labels.update(saved_labels);counter[0]=saved_counter
   failures.append((pack[name],str(p.relative_to(root)),str(e)))
 return result

failures=[];programs={}
for p in sorted((root/'scripts').rglob('*.rs2')):
 for id,start,nodes in compile_file(p):programs[id]=(start,nodes,str(p.relative_to(root)).replace('\\','/'))
out=args.output
out.parent.mkdir(parents=True,exist_ok=True)
with out.open('w',encoding='utf8',newline='\n') as f:
 f.write('# LostCityRS/Content 377-wip (May 2, 2006), commit '+commit+'\n')
 f.write('# Preservation reconstruction, not a recovered Jagex server transcript. See docs/Server/data/content/npcDialogues2006.SOURCES.md.\n')
 f.write('# NPC ID\tNode ID\tKind\tNext nodes\tText (pipe is a line break)\n')
 for id,(start,nodes,source) in sorted(programs.items()):
  f.write(f'# {id} source={source}\n{id}\t-1\tSTART\t{start}\t\n')
  for n,(kind,lines,nexts) in nodes.items():f.write(f'{id}\t{n}\t{kind}\t'+','.join(map(str,nexts))+'\t'+'\t'.join(lines)+'\n')
print('Compiled complete dialogue-only entries:',len(programs))
audit={int(r['npc_id']):r for r in csv.DictReader(args.audit.open(),delimiter='\t')} if args.audit.exists() else {}
for id,(start,nodes,source) in sorted(programs.items()):
 if id in audit and audit[id]['entry_status']=='missing':print(id,audit[id]['name'],audit[id]['spawned'],source,sep='\t')
args.rejected.parent.mkdir(parents=True,exist_ok=True)
with args.rejected.open('w',encoding='utf8') as f:
 for row in failures:f.write('\t'.join(map(str,row))+'\n')
