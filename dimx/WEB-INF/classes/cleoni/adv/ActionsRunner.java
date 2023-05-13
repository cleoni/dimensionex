package cleoni.adv;

/**
 * Parses+executes an actions block related to an event
 * Creation date: (04/08/2002 16.24.17)
 * @author: Cristiano Leoni
 */
import java.util.Vector;
/** SmallBasic scripting engine (parser). Does not parse Functions.
 * Main method is "run"
 */
public class ActionsRunner extends DimxParser {
    private String fileName = null;
    private String fileShort = null;
    //Util vectors
    
    /** Recognized instructions */
    public static final String[] setLineClosers = {CR,EOF};
    public static final String[] actKeywords =
    {"If", "Print", "Move","Broadcast","BroadcastBackground","BroadcastSound",
     "PlayBackground","PlaySound","Display","Speak","Kill","For","Debug",
     "DropItems","SendCmd","Goal","Reset","Call","Return","NewItem",
     "NewRoom","SetPanel","RefreshView","Next","UseView","Dim","Dump",
     "SetRemove","SetAdd","MoveOutside","SaveSetting","SendPage",
     "AttachEvent","Ban","PrintRight","Journal"};
     
     // Number of expected parameter for each instruction. -1 means "can vary"
     protected static final int[] setInstrArgs =
     {1,-1,-1,-1,-1,-1,
      -1,-1,-1,-1,-1,0,-1,
      -1,-1,-1,-1,1,-1,-1,
      -1,-1,-1,-1,-1,0,1,
      2,3,2,2,3,
      3,1,-1,4};
      
      
      
      /** Keywords closing an IF */
      protected static final String[] ifClosers =
      {"Else", "End_If"};
      
      protected static final String[] ifCloser =
      {"End_If"};
      
      protected static final String[] forCloser =
      {"Next"};
      
      public String actionverb = null;
      public	Token result = null; // Final result of execution - zero = failed
      
      /**
       * @param aWorld
       * @param actionsBlock
       * @param aStartLine
       * @param anOwner
       * @param anAgent
       * @param aTarget
       * @param aStackLevel
       */
      public ActionsRunner(World aWorld,Varspace aVarspace,
      String actionsBlock,
      int aStartLine,
      String aFileName,
      String aFileShort,
      String anOwner,
      AdvObject anAgent,
      String aTarget,
      int aStackLevel) {
          super(aWorld,aVarspace,aStackLevel, aStartLine, anOwner, anAgent, aTarget);
          feed(actionsBlock);
          currLine = aStartLine;
          fileName = aFileName;
          fileShort = aFileShort;
      }
      
      private boolean actAssignment(Token firstToken) throws DimxException {
          Token t = evalExpression(firstToken, 0, true /* gets ref */,"=");
          parseToken("=");
          Token res = evalExpression(lookupToken(), 0, false /* gets val */,"\n");
          logger.debug(fileShort + "(" + currLine + "): " + firstToken.strVal() + "... = " + res);
          t.assign(res,world);
          return true;
      }
      
      
      private boolean actAttachEvent(Dict params) throws DimxException {
          
          Token p0 = (Token) params.elementAt(0);
          
          AdvObject t = null;
          if (p0 != null) { // There must be a different destination
              t = world.getObject(p0);
              if (t == null) throw new DimxException("Unexistent target object: " + p0);
          }
          
          Token teventid = (Token) params.elementAt(1);
          String eventid = null;
          if (teventid != null) {
              eventid = teventid.strVal();
          }
          if (eventid == null) throw new DimxException("Event ID is missing");
          
          Token tcopyfromid = (Token) params.elementAt(2);
          if (tcopyfromid == null) {
              throw new DimxException("Source event ID is null or missing");
          }
          Token tcopyfrom = resolve(tcopyfromid.strVal(),true);
          
          if (t.isPlayer() && tcopyfrom != null && tcopyfrom.eventVal().owner != world) throw new DimxException("Attaching events of specific object to players is not allowed. Please define and attach a WORLD's event");
          
          return t.attachEvent(eventid,tcopyfromid.strVal(),tcopyfrom);
      }
      
      private boolean actBan(Dict params) throws DimxException  {
          boolean ret = true;
          world.logger.debug("Action: BAN");
          
          Token who = (Token) params.get("par0");
          
          AdvObject o = world.getObject(who);
          if (o == null) o = (AdvObject) agent;
          if (o != null && o.isPlayer()) {
              //o.display("You have been BANNED!");
              String ip = o.varGet("remoteAddr").strVal();
              world.banIp(ip);
              Player p = (Player) o;
              p.getClient().session.setCookie("control", "1"); // Remembers banning via a cookie
              world.removePeople(p,!p.isRobot(),Const.DROP_ITEMS,null);
          } else {
              world.logger.log("Cannot ban " + who + " - not a player. "+ identifyLine());
          }
          return ret;
      }

      private boolean actBroadcast(Dict params) throws DimxException  {
          world.logger.debug("WARNING: please don't use this instruction as it won't be supported in future releases");
          
          Token msg = (Token) params.get("par0");
          if (msg != null) {
              world.hear(null,msg.strVal());
          }
          return true;
      }
      private boolean actBroadcastBackground(Dict params) throws DimxException {
          
          boolean ret = true;
          
          world.logger.debug("WARNING: please don't use this instruction as it won't be supported in future releases");
          
          Token what = (Token) params.get("par0");
          Token loop = (Token) params.get("par1"); if (loop == null) loop = new Token(false);
          
          if (what != null) {
              ret = world.playBackground(what.strVal(),loop.boolVal());
          }
          
          return ret;
      }
      private boolean actBroadcastSound(Dict params) throws DimxException {
          
          boolean ret = true;
          
          world.logger.debug("WARNING: please don't use this instruction as it won't be supported in future releases");
          
          Token what = (Token) params.get("par0");
          
          if (what != null) {
              ret = world.playSound(what.strVal());
          }
          
          return ret;
      }
      private boolean actPrint(Dict params, int lineNumber, int destination) throws DimxException {
          
          int	startmsg_index = 1; // Normally 1 - can be zero
          
          Token p0 = (Token) params.elementAt(0);
          
          if ( p0.isLiteral() || params.size() == 1 ) {
              // Compact syntax - insert default destination
              shiftr(params,agent);
              p0 = (Token) params.elementAt(0);
          }
          
          int i = startmsg_index; // Default phrase to display is second parameter
          String hiddenProp = "__act" + lineNumber; // Set hidden propery name
          // Detect target's latest read phrase index
          
          DimxObject t = world;
          if (p0 != null) { // There must be a different destination
              t = world.getObjectExt(p0);
              if (t == null) {
                  world.logger.debug("WARNING: NULL destination");
                  return true;
              }
          }
          
          if (t.varExists(hiddenProp)) { // Say next
              i = ((int) t.varGet(hiddenProp).numVal())+1;
              if (i >= params.size()) {
                  i = startmsg_index; // Wrap around
              }
          }
          
          // Speak that phrase
          Token m = (Token) params.elementAt(i);
          if (m!= null) {
              String msg;
              if (m.isImage()) {
                msg = m.imageVal().toHTML("",1.0);
              } else {
                msg = m.strVal();
              }
              world.logger.debug("Displaying to " + t + ": " + msg);
              t.varGet(hiddenProp,Const.GETREF).assign(new Token(i),world);
              if (destination == Const.TO_CONSOLE) {
                  t.display(msg);
              } else {
                  t.displayRight(msg);
              }
          } else {
              world.logger.debug("actDisplay: par" + i + " is a null message - won't display");
          }
          
          return true;
      }
      
      private void actDebug(Dict params) throws DimxException {
          Token p0 = (Token) params.elementAt(0);
            world.logger.debug(p0.strVal());
            if (world.logger.debugMode != 0) { // debugMode = 0 means on file
                // Log error stack trace anyway
                world.logger.log(p0.strVal());          
            }
          //verbose=true;
          // the following triggers a nullpointer exception for debugging purposes
          // AdvObject o = null;  o.name = new Token("a");
      }

      private void actJournal(Dict params) throws DimxException {
          Token p0 = (Token) params.elementAt(0);
          Token p1 = (Token) params.elementAt(1);
          Token p2 = (Token) params.elementAt(2);
          Token p3 = (Token) params.elementAt(3);
          Journal journal = world.getCluster().journal;
            if (journal != null) { // debugMode = 0 means on file
                // Log error stack trace anyway
                journal.newItem(p0.strVal(),p1.strVal(),p2.strVal(),p3.strVal());
            }
      }
      
      private boolean actDim(Dict params) throws DimxException {
          boolean ret = true;
          boolean vars = true;
          
          do {
              String localvar = nextToken().strVal();
              //            if (varspace == null)
              //                throw new DimxException("Can't create local variable " + localvar + " - no local varspace");
              //            else {
              Token t = new Token();
              if (lookupTokenCRsens().strVal().equals("=")) {
                  parseToken("=");
                  t = evalExpression(lookupTokenCRsens(),0);
              }
              logger.debug(fileShort + "(" + currLine + "): Dim " + localvar + " = " + t);
              varspace.varSet(localvar,t);
              if (lookupTokenCRsens().strVal().equals(",")) {
                  parseToken(",");
              } else {
                  vars = false;
              }
              //            }
          } while(vars);
          return ret;
      }
      
      
      private boolean actDropItems(Dict params) throws DimxException {
          
          boolean ret = true;
          world.logger.debug("Action: DROP ITEMS");
          
          Token who = (Token) params.get("par0");
          
          AdvObject o = world.getObject(who);
          if (o == null) o = (AdvObject) agent;
          if (o != null) {
              Dict contents = o.getContents();
              if (contents.size() > 0) {
                  for (int i=contents.size()-1; i >= 0 ; i--) {
                      AdvObject what = (AdvObject) contents.elementAt(i);
                      ret = what.moveTo(o.container,agent,Const.DONT_CHECK_OPEN,Const.FORCE_REMOVE);
                  }
                  if (o.isaCharacter()) {
                      o.display(world.msgs.msg[150]);
                  }
              }
          } else {
              throw new DimxException("DropItems: cannot resolve target: >" + who + "<");
          }
          
          return ret;
      }
      
      
      private boolean actFor(Dict params) throws DimxException {
          boolean ret = true;
          
          String ts = lookupToken().strVal();
          String loopindex = null;
          Dict loopset = null; // Used in For Each
          int startVal = 0;
          int endVal = 0;
          String fortype = "FOREACH";
          if (ts.equalsIgnoreCase("Each")) {
              // For Each construct
              parseToken("Each");
              loopindex = nextToken().strVal();
              parseToken("In");
              try {
                  Token ex =  evalExpression(lookupToken(),0);
                  loopset = ex.dictVal();
                  endVal = loopset.size();
              } catch (ClassCastException e) {
                  throw new DimxException("Set expected");
              }
          } else {
              fortype = "FOR ... TO";
              loopindex = nextToken().strVal();
              parseToken("=");
              startVal = Utils.cInt(evalExpression(lookupToken(), 0).numVal());
              parseToken("To");
              endVal = Utils.cInt(evalExpression(lookupToken(), 0).numVal())+1;
              //if (startVal > endVal) world.logger.log("WARNING: FOR Start value should be less or equal to end value. " + this.printCurrLineFile());

          }
          
          StringBuffer block = new StringBuffer("");
          // Eats CR after FOR
          parseCR();
          int	blockStartLine = currLine;
          
          Dict il = parseInstructionLine(forCloser);
          String s = il.keyAt(0);
          while ( (!s.equals(EOF)) && (!s.equalsIgnoreCase("Next")) ) {
              block.append(il.elementAt(0));
              il = parseInstructionLine(forCloser);
              s = il.keyAt(0);
          }
          
          parseToken("Next");
          
          String x = block.toString();

          if (!x.equals("")) {
              
              if (fortype.equals("FOREACH")) {
                  for (int i=startVal; i<endVal; i++) {
                      world.logger.debug("Looping. Set element # " + i);
                      resolve(loopindex,true).assign((Token) loopset.elementAt(i),world);

                      // Final part is equal for both
                      ActionsRunner runner = new ActionsRunner(world, varspace, x, blockStartLine, fileName, fileShort, owner, agent, target, stackLevel+1);
                      runner.run();
                      if (runner.actionverb.equalsIgnoreCase("Return")) {
                          ret = false;
                          result = runner.result; // Propagate result
                          actionverb = "Return"; // Propagate return statement
                          break;
                      }
                      // End equal part
                      
                      endVal = loopset.size();
                  }
              } else { // Normal For ... To
                  for (int i=startVal; i<endVal; i++) {
                      world.logger.debug("Looping. Index= " + i);
                      resolve(loopindex,true).assign(new Token(i),world);

                      // Final part is equal for both
                      ActionsRunner runner = new ActionsRunner(world, varspace, x, blockStartLine, fileName, fileShort, owner, agent, target, stackLevel+1);
                      runner.run();
                      if (runner.actionverb.equalsIgnoreCase("Return")) {
                          ret = false;
                          result = runner.result; // Propagate result
                          actionverb = "Return"; // Propagate return statement
                          break;
                      }
                      // End equal part
                  }
              }
          }
          return ret;
      }
      private boolean actGoal(Dict params) throws DimxException  {
          String agentname = "(unknown)";
          if (agent != null) agentname = agent.getName();
          String message = null;
          Token p0 = (Token) params.elementAt(0);
          if (p0 != null) message = p0.strVal();
          
          return world.goal(agentname,message);
      }
      
/*
 * Executes an If..Else construct.
 * the IF condition has been already consumed (passed in par0
 * CR is still there
 *
 */
      private boolean actIfElse(Dict params) throws DimxException {
          
          boolean ret = true;
          
          // Prepare space for blocks
          StringBuffer thenblock = new StringBuffer("");
          int	thenStartLine = currLine+1;
          StringBuffer elseblock = new StringBuffer("");
          int	elseStartLine = 0;
          
          // Parses and eats optional THEN
          //Token firstToken = lookupTokenCRsens();
          //if (firstToken.strVal().equalsIgnoreCase("Then")) parseToken("Then");
          
          // Eats first CR
          parseCR();
          
          // Read first block until closing
          Dict il = parseInstructionLine(ifClosers);
          String s = il.keyAt(0);
          while ( (!s.equals(EOF)) && (!Utils.isIn(s,ifClosers)) ) {
              thenblock.append(il.elementAt(0));
              il = parseInstructionLine(ifClosers); // or Else
              s = il.keyAt(0);
          }
          
          // Else block encountered
          if (s.equalsIgnoreCase("Else")) {
              parseToken("Else"); parseCR();
              // Read else block now
              elseStartLine = currLine;
              il = parseInstructionLine(ifCloser);
              s = il.keyAt(0);
              while ( (!s.equals(EOF)) && (!Utils.isIn(s,ifCloser)) ) {
                  elseblock.append(il.elementAt(0));
                  il = parseInstructionLine(ifCloser);
                  s = il.keyAt(0);
              }
          }
          
          parseToken("End_If");
          
          // Evaluate condition
          Token expr = (Token) params.get("par0");
          String x = null;	// Block of instructions to be executed
          int x_startLine = 0;	// Starting line of that block
          if (expr.boolVal()) {
              world.logger.debug("TRUE");
              x = thenblock.toString();
              x_startLine = thenStartLine;
          } else {
              world.logger.debug("FALSE");
              x = elseblock.toString();
              x_startLine = elseStartLine;
          }
          
          if (!x.equals("")) {
              ActionsRunner runner = new ActionsRunner(world, varspace, x, x_startLine, fileName, fileShort, owner, agent, target, stackLevel+1);
              runner.run();
              if (runner.actionverb == null || runner.actionverb.equalsIgnoreCase("Return")) {
                  ret = false;
                  result = runner.result; // Propagate result
                  actionverb = "Return"; // Propagate return statement
              }
          }
          return ret;
      }
      
      private boolean actKill(Dict params) throws DimxException  {
          
          boolean ret = true;
          
          Token who = (Token) params.get("par0");
/*
        String victimId = null;
        if (who != null) {
                victimId = who.strVal();
        }
 
        if (victimId == null) victimId = agent;
 
        AdvObject o = world.getObject(victimId); */
          
          if (who == null) who = new Token(agent);
          AdvObject o = world.getObject(who);
          if (o != null) {
              if (o.isanItem()) {
                  boolean prevee = world.eventsEnabled;
                  world.eventsEnabled = false;
                  ((Item) o).die(true,0);
                  world.eventsEnabled = prevee;
                  ret = true;
              } else if (o.isaRoom()) {
                  ret = world.removeRoom((Room) o);
              } else if (o.isaCharacter()) {
                  Character c = (Character) o;
                  AdvObject ao = agent;
                  if (ao != null) {
                      String agentname = ao.getName();
                      c.display(world.msgs.actualize(world.msgs.msg[152],agentname));
                  }
                  world.removePeople(c,!c.isRobot(),Const.DROP_ITEMS,null);
                  ret = true;
              } else {
                  throw new DimxException("Could not kill object: >" + who + /*victimId + */"< - invalid type");
              }
          } else {
              world.logger.log("You tried to Kill a NULL object: " + who + " \n at: " + identifyLine());
              //throw new DimxException("Could not kill object: >"+ who + /*victimId + */"< - un-existent");
          }
          
          return ret;
      }
      private void actMove(Dict params) throws NestedException,DimxException {
          
          Token twhat = (Token) params.get("par0");
          Token twhere = (Token) params.get("par1");
          
          AdvObject what = world.getObject(twhat);

          if (what == null) {
              world.logger.log("You tried to Move a NULL object" + identifyLine());
              // throw new DimxException("You tried to Move a NULL object");
          }
          try {
              if (what != null) {
                  what.moveTo(world.getObject(twhere),agent,Const.DONT_CHECK_OPEN,Const.CHECK_EVENTS);
              }
          } catch (DimxException exc) {
              // Already documented - pass on as Nested
              throw new NestedException(exc.getMessage());
          }
      }
      private boolean actMoveOutside(Dict params) throws DimxException {
          world.logger.debug("MOVE OUTSIDE");
          Token twhat = (Token) params.get("par0");
          Token twhere = (Token) params.get("par1");
          
          String wheres = twhere.strVal();
          
          AdvObject what = world.getObject(twhat);
          
          if (what != null) {
              // Object found
              String result = world.moveOutside(what,wheres);
              if (result != null) {
                  // There was a problem
                  world.logger.debug("There was a problem: " + result);
                  AdvObject ao = agent;
                  if (ao != null && ao.isPlayer()) {
                      ((Player) ao).display(world.msgs.actualize(world.msgs.msg[181],result));
                      ((Player) ao).display(world.getCluster().htmlPullUp(world.slot));
                  } else {
                      world.logger.log("Cannot perform inter-dimensional jump for object: " + what + " result: " + result);
                      //throw new DimxException("Cannot perform inter-dimensional jump: " + result);
                  }
              }
          } else {
              throw new DimxException("Undefined object: " + twhat);
          }
          return true;
      }
      private boolean actNewRoom(Dict params) throws DimxException {
          boolean ret = true;
          
          Token t = (Token) params.get("par0");
          
          if (t != null) {
              String id = t.strVal();
              
              if (id.equals("")) throw new DimxException("Missing id for new room");
              
              if (world == null) logger.debug("AAAAAAAAAAAAARRRRRRRRRRRGH");
              
              Room x = new Room(world,id,id,"",null,null);
              
              world.addRoom(x,false);
          } else {
              throw new DimxException("Missing id for new room");
          }
          
          return ret;
      }
      private boolean actPlayBackground(Dict params) throws DimxException {
          
          Token p0 = (Token) params.elementAt(0);
          
          if ( p0.isLiteral() || (world.getObjectExt(p0) == null) ) {
              // Compact syntax - insert default destination
              shiftr(params,agent);
              p0 = (Token) params.elementAt(0);
          }
          
          DimxObject t = world;
          if (p0 != null) { // There must be a different destination
              t = world.getObjectExt(p0);
              if (t == null) {
                  world.logger.debug("WARNING: NULL destination");
                  return true;
              }
          }
          
          // Send that background sound!
          Token m = (Token) params.elementAt(1);
          Token loop = (Token) params.elementAt(2);
          if (loop == null) loop = new Token(false);
          if (m!= null) {
              String what = m.strVal();
              world.logger.debug("actPlayBackground: background sound to " + t + ": " + what );
              t.playBackground(m.strVal(),loop.boolVal());
          }
          
          return true;
      }
      private boolean actPlaySound(Dict params) throws DimxException {
          
          Token p0 = (Token) params.elementAt(0);
          
          if ( p0.isLiteral() || (world.getObjectExt(p0) == null) ) {
              // Compact syntax - insert default destination
              shiftr(params,agent);
              p0 = (Token) params.elementAt(0);
          }
          
          DimxObject t = world;
          if (p0 != null) { // There must be a different destination
              t = world.getObjectExt(p0);
              if (t == null) {
                  world.logger.debug("WARNING: NULL destination");
                  return true;
              }
          }
          
          // Send that sound!
          Token m = (Token) params.elementAt(1);
          if (m!= null) {
              String what = m.strVal();
              world.logger.debug("actPlaySound: playing sound to " + t + ": " + what );
              t.playSound(what);
          }
          
          return true;
      }
      private boolean actReset(Dict params) {
          
          boolean ret = true;
          
          world.logger.debug("Action: RESET");
          
          world.reset = true;
          
          return ret;
      }
      
      private boolean actSaveSetting(Dict params) throws DimxException {
          boolean ret = true;
          
          String key = ((Token) params.elementAt(0)).strVal();
          
          Token tval = (Token) params.elementAt(1);
          String val = null;
          
          if (tval.isDict()) {
              val = tval.dictVal().toSettingsPair(",");
          } else {
              val = tval.strVal();
          }
          
          world.saveSetting(key,val);
          
          return ret;
      }
      
      private boolean actSendCmd(Dict params) throws DimxException {
          boolean ret = false;
          
          Token p0 = (Token) params.elementAt(0);
          
          if ( p0.isLiteral() || (world.getObjectExt(p0) == null) ) {
              // Compact syntax - insert default destination
              shiftr(params,agent);
              p0 = (Token) params.elementAt(0);
          }
          
          DimxObject t = world;
          if (p0 != null) { // There must be a different destination
              t = world.getObjectExt(p0);
              if (t == null) {
                  world.logger.debug("WARNING: NULL destination");
                  return true;
              }
          }
          
          // Send that cmd!
          Token m = (Token) params.elementAt(1);
          if (m!= null) {
              String cmd = m.strVal();
              if (cmd.equals("")) {
                  world.logger.log("ERROR: Caught null command in actSendCmd. Params=" + params);
                  throw new DimxException("Null command in sendCmd");
              }
              world.logger.debug("SendCmd: sending command: " + cmd + " to " + t );
              ret = t.sendCmd(cmd);
          }
          
          return ret;
      }
      
      private boolean actSetPanel(Dict params) throws DimxException {
          boolean ret = true;
          String panelId = "";
          
          if (params.size() == 1) { // Compact syntax - insert agent
              shiftr(params,agent);
          };
          
          Token p0 = (Token) params.elementAt(0);
          Token p1 = (Token) params.elementAt(1);
          
          if (p1 != null) panelId = p1.strVal();
          
          if (panelId.equals("")) throw new DimxException("Missing parameter: panelId");
          
          Panel panel = (Panel) world.getPanel(panelId);
          
          if (panel == null)
              throw new DimxException("Unexistent panel: " + panelId);
          
          Dict people = null;
          
          DimxObject t = world;
          if (p0 != null) { // There must be a different destination
              t = world.getObjectExt(p0);
              if (t == null) {
                  world.logger.debug("WARNING: NULL destination");
                  return true;
              }
          }
          
          ret = t.setPanel(panelId);
          
          return ret;
      }
      
      private boolean actSetAdd(Dict params) throws DimxException {
          Token p1 = (Token) params.elementAt(0);
          Token p2 = (Token) params.elementAt(1);
          Token p3 = (Token) params.elementAt(2);
          if (!p1.isDict()) throw new DimxException("SET argument expected");
          Dict d1 = p1.dictVal();
          d1.put(p2.strVal(),p3);
          return true;
      }
      
      private boolean actSetRemove(Dict params) throws DimxException {
          Token p1 = (Token) params.elementAt(0);
          Token p2 = (Token) params.elementAt(1);
          if (!p1.isDict()) throw new DimxException("SET argument expected");
          Dict d1 =  p1.dictVal();
          if (d1.remove(p2.strVal())) {
              return true;
          } else {
              logger.debug("SetRemove: element not found: " + p2.strVal() + " in: " + p1);
              return true;
          }
      }
      
      private boolean actSpeak(Dict params, int lineNumber) throws DimxException {
          
          int	startmsg_index = 2; // Normally 2 - can be zero
          
          Token p0 = (Token) params.elementAt(0);
          Token p1 = null;
          
          if ( p0.isLiteral() || (world.getObjectExt(p0) == null) ) {
              // Compact syntax - insert default speaker and destination
              shiftr(params,agent);
              shiftr(params,owner);
          } else {
              // p0 is an id - assume it is dest
              p1 = (Token) params.elementAt(1);
              if (p1.isLiteral() || (world.getObjectExt(p1) == null)) {
                  // from is missing - insert default speaker
                  shiftr(params,owner);
              }
          }
          
          p0 = (Token) params.elementAt(0);
          p1 = (Token) params.elementAt(1);
          
          int i = startmsg_index; // Default phrase to display is second parameter
          String hiddenProp = "__act" + lineNumber; // Set hidden propery name
          // Detect target's latest read phrase index
          
          AdvObject s = null;
          if (p0 != null) { // There must be a different speaker
              s = world.getObject(p0);
          }
          if (s != null && !s.isaCharacter()) { // Invalid speaker
              world.logger.debug("Warning: invalid speaker: " + p0.strVal());
              s=world.defaultCharacter;
          }
          
          DimxObject t = world;
          if (p1 != null) { // There must be a different destination
              t = world.getObjectExt(p1);
              if (t == null) {
                  world.logger.debug("WARNING: NULL destination");
                  return true;
              }
          }
          
          if (t.varExists(hiddenProp)) { // Say next
              i = ((int) t.varGet(hiddenProp).numVal())+1;
              if (i >= params.size()) {
                  i = startmsg_index; // Wrap around
              }
          }
          
          // Speak that phrase
          Token m = (Token) params.elementAt(i);
          if (m!= null) {
              String msg = m.strVal();
              world.logger.debug("Speaking parameter #" + i + " to " + t + ": " + msg);
              t.varGet(hiddenProp,Const.GETREF).assign(new Token(i),world);
              
              if (msg.equals("*")) { // Speech finished!
                  world.fireEvent("onSpeechFinish",(AdvObject) s,t.id,t.id,false);
              } else {
                  boolean eventsBefore = world.eventsEnabled;
                  world.eventsEnabled = false; // Disable events to avoid cascading onHear
                  t.hear(s,msg);
                  world.eventsEnabled = eventsBefore;
              }
          } else {
              world.logger.debug("actSpeak: par" + i + " is a null message - won't speak");
          }
          
          return true;
      }
      
      private boolean actSendPage(Dict params) throws DimxException {
          boolean ret = false;
          
          Token p0 = (Token) params.elementAt(0);
          
        /* recycled code to be adapted later
        if ( p0.isLiteral() || (world.getObjectExt(p0.strVal()) == null) ) {
                // Compact syntax - insert default destination
                shiftr(params,agent);
                p0 = (Token) params.elementAt(0);
        }
         */
          
          DimxObject t = world;
          if (p0 != null) { // There must be a different destination
              t = world.getObjectExt(p0);
              if (t == null) {
                  world.logger.debug("WARNING: NULL destination");
                  return true;
              }
          }
          
          // Send that view!
          Token m = (Token) params.elementAt(1);
          if (m!= null && !m.strVal().equals("")) {
              Page view = m.valPage();
              world.logger.debug("Sending page: " + view.id + " to " + t );
              
              if (view != null) {
                  ret = t.useView(view);
                  int msecs = Utils.cInt(((Token) params.elementAt(2)).numVal()) * 1000;
                  if (msecs > 30000) {
                      world.logger.debug("interval parameter too big - assuming it is already expressed in milliseconds");
                      msecs = msecs / 1000;
                  }
                  if (msecs > 0)  {
                      t.sendCmd("custom:timed!" + msecs + ",refresh!scene");
                  }
              } else {
                  throw new DimxException("Unexistent page: " + view.id);
              }
          // we may be resetting world view to default
          } else if ((t == world) && (m!= null && m.strVal().equals(""))) { 
              world.logger.debug("Resetting World page to standard view");
              ret = world.useView(null);
              int msecs = Utils.cInt(((Token) params.elementAt(2)).numVal()) * 1000;
              if (msecs > 30000) {
                  world.logger.debug("interval parameter too big - assuming it is already expressed in milliseconds");
                  msecs = msecs / 1000;
              }
              if (msecs > 0)  {
                  t.sendCmd("custom:timed!" + msecs + ",refresh!scene");
              }
          }else {          
              throw new DimxException("Undefined view.");
          }
          
          return ret;
      }
      
/*
 * Quickly Parses a If Else structure (no execution) where the 1st line was already consumed
 *
 */
      private String parseIfElse() throws DimxException {
          
          StringBuffer sbf = new StringBuffer("");
          Dict il = parseInstructionLine(ifClosers);
          String s = il.keyAt(0);
          while ( (!s.equals(EOF)) && (!Utils.isIn(s,ifClosers)) ) {
              sbf.append(il.elementAt(0));
              il = parseInstructionLine(ifClosers); // or Else
              s = il.keyAt(0);
          }
          
          if (s.equalsIgnoreCase("Else")) {
              // Read and consume Else
              sbf.append(readToCR() + "\n");
              il = parseInstructionLine(ifCloser);
              s = il.keyAt(0);
              while ( (!s.equals(EOF)) && (!Utils.isIn(s,ifCloser)) ) {
                  sbf.append(il.elementAt(0));
                  il = parseInstructionLine(ifCloser);
                  s = il.keyAt(0);
              }
          }
          
          if (!s.equalsIgnoreCase("End_If")) {
              throw new DimxException("End_If missing! FOUND:" + s);
          } else {
              sbf.append(readToCR()+"\n");
          }
          
          if (verbose) world.logger.debug("Parsed nested IF: \n----\n" + sbf.toString() + "---\ncurrLine=" + currLine);
          return sbf.toString();
      }
      /**
       * Parses and instruction line. The specified closing Token will not be consumed
       * @throws DimxException
       * @return
       */
      public Dict parseInstructionLine(String[] closers) throws DimxException {
          boolean running = true;
          int parsedLine = currLine;
          StringBuffer sbuf = new StringBuffer();
          
          Token firstToken = lookupTokenCRsens();
          String actionverb = firstToken.strVal();
          
          if (!actionverb.equals(EOF)) {
              // Read and recognize Action verb
              if (verbose) world.logger.debug("Parsing line " + parsedLine);
              
              if (!firstToken.isIdentifier() && !firstToken.strVal().equals(CR)) {
                  throw new DimxException("Instruction or identifier expected instead of: \"" + actionverb + "\"");
              }
              if (!Utils.isIn(actionverb,actKeywords) && !Utils.isIn(actionverb,closers)) {
                  // Assume assignment
                  actionverb = "Assignment";
              }
              
              if (!Utils.isIn(actionverb,closers)) { // Not a closer? Consume it
                  String xx = readToCR();
                  sbuf.append(xx);
                  sbuf.append("\n");
              }
              
              if (actionverb.equalsIgnoreCase("If")) {
                  // Go on appending until closed
                  sbuf.append(parseIfElse());
              } 
          }
          Dict result = new Dict();
          result.put(actionverb,sbuf.toString());
          if (verbose) world.logger.debug("Parsed line " + parsedLine + ": " + sbuf.toString() + " Now at "+ currLine);
          return result;
      }
      
      /**
       * @throws DimxException
       * @return
       */
      public Token run() throws DimxException {
          boolean running = true;
          //verbose = true;
          world.logger.incIndent();
          world.logger.debug(fileShort + "(" + currLine + "): running");
          try {
              if (stackLevel > 255) {
                  throw new DimxException("Too many nested calls.");
              }
              
              while (running) {
                  if (!lookupToken().strVal().equals(EOF)) {
                      if (verbose) world.logger.debug("ActionRunner.run: Executing line " + currLine);
                      if (verbose) world.logger.debug("ActionRunner.run: Actual char=>" + buf.charAt(startToken) + "<=");
                      
                      Dict params = new Dict();
                      
                      // Read and recognize Action verb
                      Token firstToken = lookupToken();
                      actionverb = firstToken.strVal();
                      if (!firstToken.isIdentifier()) {
                          throw new DimxException("Instruction or identifier expected instead of: \"" + actionverb + "\"");
                      }
                      Vector res = identifyFunProc(actionverb,actKeywords,setInstrArgs);
                      int no_params = ((Integer) res.elementAt(0)).intValue();
                      Event e = (Event) res.elementAt(1);
                      if (no_params == Const.NOTFOUND) {
                          // Instruction not found - Assume assignment
                          actionverb = "Assignment";
                          no_params = 0; // expression to be read later
                      } else {
                          firstToken = nextToken(); // eat
                      }
                      // Handle "special" instructions
                      if (actionverb.equalsIgnoreCase("CALL")) {
                          world.logger.debug(actionverb.toUpperCase() + " " + lookupElement(setTabSpace).strVal() + "..." );
                      } else if (actionverb.equalsIgnoreCase("IF")) {
                          world.logger.debug("IF " + lookupElement(setTabSpace).strVal() + "...");
                      } else if (actionverb.equalsIgnoreCase("For")) {
                          //verbose = true;
                      }
                      
                      if (no_params != 0) {
                          // no_params = -1 means variable-sized list of params
                          params = parseList(no_params,setLineClosers);
                      }
                      //if (no_params == 0) {
                      // In case of no params, this print might be misleading
                      //world.logger.debug(actionverb + " " + lookupToken().strVal() + "...");
                      //}
                      
                      // Execution
                      String action_lowcase = actionverb.toLowerCase();
                      if (action_lowcase.equals("if")) {
                          running = actIfElse(params);
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("assignment")) {
                          running = actAssignment(firstToken);
                      } else if (action_lowcase.equals("for")) {
                          running = actFor(params);
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("dim")) {
                          running = actDim(params);
                      } else 	if (action_lowcase.equals("return")) {
                          if (world.logger.on) world.logger.debug(actionverb.toUpperCase() + params);
                          running = false;
                          result = (Token) params.get("par0");
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("print") || action_lowcase.equals("display")) {
                          if (world.logger.on) world.logger.debug(actionverb.toUpperCase() + params);
                          running = actPrint(params,currLine,Const.TO_CONSOLE);
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("printright")) {
                          if (world.logger.on) world.logger.debug(actionverb.toUpperCase() + params);
                          running = actPrint(params,currLine,Const.TO_RIGHTCOLUMN);
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("speak")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actSpeak(params,currLine);
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else if (action_lowcase.equals("broadcast")) {
                          if (world.logger.on) world.logger.debug(actionverb.toUpperCase() + params);
                          running = actBroadcast(params);
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("playbackground")) {
                          if (world.logger.on) world.logger.debug(actionverb.toUpperCase() + params);
                          running = actPlayBackground(params);
                          if (world.logger.on) world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("broadcastbackground")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actBroadcastBackground(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("playsound")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actPlaySound(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("broadcastsound")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actBroadcastSound(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("dropitems")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actDropItems(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("move")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          actMove(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("kill")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actKill(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("sendcmd")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actSendCmd(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("refreshview")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          params.put("par1",new Token(Const.CMDE_REFRSCENE));
                          running = actSendCmd(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("sendpage")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          actSendPage(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("useview")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          world.logger.debug("WARNING - UseView is no more supported - use SendPage instead - check Developers Reference for changes in interval parameter");
                          actSendPage(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("goal")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actGoal(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("reset")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actReset(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("call")) {
                          //running = actCall(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else if (action_lowcase.equals("setpanel")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actSetPanel(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else if (action_lowcase.equals("newitem")) {
                          throw new DimxException("The NewItem instruction is no more supported - please use the NewItem FUNCTION instead - check Developers Reference for details.");
                      } else if (action_lowcase.equals("setremove")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          actSetRemove(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else if (action_lowcase.equals("setadd")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          actSetAdd(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("attachevent")) {
                          running = actAttachEvent(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else if (action_lowcase.equals("newroom")) {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          running = actNewRoom(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("moveoutside")) {
                          running = actMoveOutside(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("savesetting")) {
                          running = actSaveSetting(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("debug")) {
                          actDebug(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else 	if (action_lowcase.equals("ban")) {
                          running = actBan(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                       } else 	if (action_lowcase.equals("journal")) {
                          actJournal(params);
                          world.logger.debug(fileShort + "(" + currLine + "): " + actionverb.toUpperCase() + " executed");
                      } else {
                          world.logger.debug(actionverb.toUpperCase() + params);
                          throw new DimxException("Unmanaged statement: \"" + actionverb + "\" (engine code missing)");
                      }
                      parseCREOF();
                  } else {
                      running = false;
                  }
              }
          } catch (NestedException e) {
              // It is already fully defined - simply pass it on
              world.logger.decIndent();
              if (stackLevel > 0) {
                  // If nested parsing then simply pass on exception
                  throw new NestedException(e.getMessage());
              } else {
                  // Last step - revert to DimxException
                  throw new DimxException(e.getMessage());
              }
          } catch (DimxException e) {
              //e.printStackTrace(System.out);
              String errmsg = "ERROR executing script\n** " + e.getMessage() + "\nnear line: " + currLine;
              if (fileName != null && !world.hideSourcePath) errmsg = errmsg + " in file: " + fileName;
              errmsg = errmsg + "\n" + identifyLine();
              world.logger.decIndent();
              if (stackLevel > 0) {
                  // If nested parsing then define problem and pass to upper level
                  throw new NestedException(errmsg);
              } else {
                  // No nesting - Just define problem
                  throw new DimxException(errmsg);
              }
          } catch (Exception e) {
              world.logger.debug("ERROR executing ACTIONS block" );
              world.logger.debug(e);
              world.logger.debug("at line: " + currLine);
              if (!world.hideSourcePath) world.logger.debug(" file: " + fileName + "\n");
              world.logger.debug(identifyLine());
              String x = e.toString();
              StringBuffer errmsg = new StringBuffer("ERROR executing ACTIONS block\n**" + x + "\n");
              errmsg.append("(See log for stack trace dump)\n");
              errmsg.append("at line: " + currLine);
              if (!world.hideSourcePath) errmsg.append(" file: " + fileName + "\n");
              errmsg.append(identifyLine());
              world.logger.decIndent();
              if (stackLevel > 0) {
                  // If nested parsing then define problem and pass to upper level
                  throw new NestedException(errmsg.toString());
              } else {
                  // No nesting - Just define problem
                  throw new DimxException(errmsg.toString());
              }
          }
          world.logger.decIndent();
          if (result == null) result = new Token(true); // Succeed by default
          world.logger.debug("Returning: " + result);
          return result;
      }
      private void shiftr(Dict params,String what) {
          params.insertElementAt(new Token(what),0);
      }
 
      private void shiftr(Dict params,DimxObject what) {
          params.insertElementAt(new Token(what),0);
      }

      private String printCurrLineFile() {
          StringBuffer sourcepath = new StringBuffer("");
          if (!world.hideSourcePath) sourcepath.append(" file: " + fileShort);
           return "At line " + currLine + sourcepath.toString();
      }
      
}
