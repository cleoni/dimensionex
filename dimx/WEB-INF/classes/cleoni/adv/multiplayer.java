package cleoni.adv;


import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/** The Main Game Engine Servlet.
 * Main method is "service".
 * Everything starts actually from there.
 *
 * Recent History:
 *
 * 7.1.4 - 2023-05-24
 *      bug fixed: using image names beginning with x caused a glitch in loading data
 * 7.1.3 - 2023-05-23
 *      auto selection of best resolution if top frame has bestRes() function
 *      some kind of responsive behaviour with auto selection of bast client for browser orientation
 *      dropped soundpad view (client now uses javascript audio play function)
 * 7.1.2 - 2023-05-18
 *      improved music play system (non more popup window)
 *      fix in absolutized urls (double slash removed)
 *      fixed buttons rendering (auto removal of {} in style specs)
 *      controls are now resizable on mobile devices
 * 7.1.1 - 2023-05-17
 *      discarded: checkuser,createuser,checkpass,getavatar,binduser
 *      discarded: banner view
 *      added res: 400x800 (patched) for cellphons
 * 7.1.0 - 2023-05-16
 *      click hidden command added to standard panel
 *      onLook now uses also $TARGET="explicit" in case explicit look via button.
 *      Discarded hooks for language parser classes
 * 7.0.8 - 2023-05-12
 *      World ticks won't happen during ajax calls anymore
 * 7.0.7 - 2023-05-10
 *      Hardened stopwords system
 * 7.0.6 - 2023-05-04
 *      Disabled output on browser info when looking a human player
 * 7.0.5 - 28/04/2023
 *      few fixes of old http:// urls. Removed SWF support
 * 7.0.4 - 22/04/2023
 *      new World setting for tick length in seconds (tickLength, default=30)
 * 7.0.3 - 24/01/2023
 *      fixes
 * 7.0.2 - 22/01/2023
 *      fix for Must specify an http:// URL in HttpFetch
 *      fix in "unable to load cluster" error message for security
 * 7.0.1 - 11/08/2022
 *      fixes for sound/music and should run correctly under https
 * 6.5.0a
 *      Bug fixed: Map bug in embed - map position was not updated when refreshing when game was embedded (thanks Cotteux for this)
 *      Bug fixed: Show map was producing a NullPointer error when no specific panel was selected
 *      Bug fixed: Admin panel, snapshot view, log viewer and optimizer view did not include skin's stylesheet
 * 6.4.9e, 6.5.0
 *      Added support for Google Analytics (analytics world settings)
 *      Released as 6.5.0
 * 6.4.9d
 *      Added function: Urlencode()
 * 6.4.9c
 *      Fix for stylesheet support in HOF view, maint/clean profile to include fbid info
 * 6.4.9b
 *      Fix nel caricamento delle skins
 * 6.4.9a
 *      Fixes in createProfile method
 * 6.4.8e, 6.4.9
 *      Fixes to recently added views and added binduser view to bind to Facebook profile
 *      Released as 6.4.9
 * 6.4.8d
 *      Fixed call for checkpass and added getavatar view to get the player's avatar
 * 6.4.8c
 *      Supports offline profile creation and checking profile existence: views checkuser, checkpass and createuser
 * 6.4.8b
 *      Modified generation of sound-ebbeding code to make it compatible with Firefox and Chrome
 * 6.4.8a
 *      Added Journal instruction and journal world setting
 * 6.4.7h
 *      Added monitoring code in a cluster-related problem
 *      Fixed problems due to Token.dictVal() returning a NULL (hope new strange effects won't pop up)
 *      Released as 6.4.8
 * 6.4.7g
 *      Fixed bug in string concatenation operator "&"
 * 6.4.7f
 *      Fixed bug in object.setProperty
 * 6.4.7e
 *      ?
 * 6.4.7d
 *      Added methods: object.getProperty(property) / setProperty(property,value)
 * 6.4.7c
 *      Fixed NullPointer in Dict.java / indexOf
 * 6.4.7b
 *      Fixed problem in itemPlace if the item was removed during onReceive event
 * 6.4.7a
 *      Added event: beforeOpen()
 *      Fixed onReceiveItem bug - item was not restored to its original state
 * 6.4.6e-6.4.7
 *      Fixed issues with resolution choice
 *      Added "locked" parameter to lock pre-defined resolution
 *      Released as 6.4.7
 * 6.4.6d
 *      Added tolerancy for wrong atribute list in rooms definition (warning is generated)
 * 6.4.6c
 *      Fixed a further bug in Utils.stringSplit
 * 6.4.6b
 *      Fixed a (horrible) double-delimiter bug in Utils.stringSplit
 * 6.4.6a
 *      Enhancement: ATTRLIST support for the WORLD section.
 * 6.4.5h
 *      Bug fix: The screen resolution drop-down did not preset according to the width/height URL parameters. Fixed.
 * 6.4.5g
 *      Bug Fix: Locating Skins relative to ServletContext RealPath.
 * 6.4.5f
 *      Enhancement: Maintenance panel has now a "Server reload settings" option
 *      Enhancement: Added savingDir setting in dimensionex.properties to allow saving of profiles in an alternate folder
 * 6.4.5e
 *      Bug Fix: Removed BASE HREF tag from customView HTML.
 *      Added standard Page Transistion 0.5 to persistent $WORLD custom views.
 * 6.4.5d
 *      Reverted core loop in multplayer.java back to 6.4.4, adapted for custom language processor
 *      Removed extra fields in Utils that were used as temp storage
 *      Removed cmdXXX functions - now useless
 *      Modified ISayProcessor prototype so that returns a Vector of values and accepts less input
 *      Modified CustCmdProcessor to comply the new prototype - added packResults utility function in it (possibly to be copied in subclasses)
 * 6.4.5c
 *      Enhancement: Added $World support for SendPage to support permanent custom World view.
 * 6.4.5b
 *      Enhancement: Added support for $banner in HTML Templates
 * 6.4.5a
 *      Bug Fix: Embedded games did't properly work if used inside IE7 (P3P header was needed)
 * 6.4.4n
 *      Bug fixed: Save and Exit was not working anymore
 *      Bug fixed: Skins were loaded from DimX server's filesystem even for remotely loaded games
 *      Released as 6.4.5
 * 6.4.4m
 *      enhancement: Added Asc() function
 *      enhancement: Added "Mod" operator for arithmetic modulus
 *      Bug fix: Concat "&" operator was not working
 * 6.4.4L
 *      Maintenance servlet: cleaning of "_login" setting was missing - fixed
 *      Bug fixed: An empty If ... End_If block was causing a NullPointer
 * 6.4.4k
 *      Fixed problem in clientScript loading.
 * 6.4.4j
 *      Enhancement: Moving in-line command processing code to reusable function.
 *      Accomodating what should be the last ISayProcessor API signature changes.
 * 6.4.4i
 *      Enhancement: Added cmdUSE, cmdGIVE, cmdPUT, and cmdHIDE
 *      Moving in-line command processing code to reusable function.
 * 6.4.4h
 *      Enhancement: Added cmdLOOK, cmdSEARCH, cmdOPEN, cmdCLOSE, cmdSAY. 
 *      Moving in-line command processing code to reusable function.
 * 6.4.4g
 *      Enhancement: Added cmdROTATE, cmdPICK, cmdDROP. 
 *      Moving in-line command processing code to reusable function.
 * 6.4.4f
 *      Enhancement: Added support for clientScript in world properties definition.
 *      This primarily allows debugging one game while others run normally.
 * 6.4.4e
 *      Enhancement: Added cmdGO. Moving in-line "GO" command processing code to reusable function.
 * 6.4.4d
 *      Enhancement: Added msg #184 "is in" for players view
 *      Fix: Restored FALSE=FAILED return status for moveTo function (AdvObject.java:980) in case target container has not enough space - hope new messages don't pop up now
 * 6.4.4c
 *      Enhancement: Added gameinfo("navigator") function to return current url
 * 6.4.4b
 *      Enhancement: Added natural logarithm function Log()
 * 6.4.4a
 *      Bug fix: Bug in area change management caused visualization flaws and potentially other weird side effects
 *	Updated: msgs_eng, msgs_ita, maintpage.htm, worldnav11.properties
 * 6.4.3d
 *      Bug fix: dummy string "(description)" removed from player's description
 *      released as 6.4.4
 * 6.4.3c
 *      Enhancement: Screen resolution is stored in users' cookies
 *      Enhancement: Skin loader low loads skins directly from the 'skins' folder
 * 6.4.3b
 *      Bug fix: Fixed description display glitch after area change
 *      Enhancement: Extended @description system to all objects
 *      Bug fix: Parser did tolerate calling function with less parameters than expected - fixed
 *      Enhancement: String concat operator '&'
 * 6.4.3a
 *      Bug fix: Message 181 did not show up - fixed
 *      maintenance Panel: Added option for deleting profile with login before date
 *      Bug fix: Some fixing on the admin panel for helping password remembering
 * 6.4.2e
 *      Enhancement: Added dummy username field in admin panel to allow password saving
 *      Enhancement: Added getPlayer(name) function
 *      Enhancement: Added messages: 181 and 182
 *      Bug fix: Token did not tolerate null AdvObjects
 *      Released as 6.4.3
 * 6.4.2d
 *      Bug fix: Change on Kill operation for leaving contained items intact was affecting the area change: items were erroneously dropped when traversing the passage. 
 * 6.4.2c
 *      Enhancement: Added ZOOMIMAGE to ITEM objects
 *      Bug fix: Kill instruction used on items was killing inner objects - now they are dropped to the floor
 *      Bug fix: producing a "null" saved restoreinfo if the item was autorestored
 * 6.4.2b
 *      Enhancement: added double click on "pick" for "pick all"
 *      Fixed DictEnumerator for cycles - clones the Dict automatically
 *      Fixed format of _login setting to conform with _when setting
 *      Fixed output of dynamic room's description, 2 trailing newlines now get removed
 * 6.4.2a
 *      Source code never released in CVS
 * 6.4.1c
 *      Enhancement: Support for @description for rooms
 *      Bug fixed: Support for special variables $OWNER, $AGENT and $TARGET in custom panels was not correct in some cases. Fixed.
 *      Released as 6.4.2
 * 6.4.1b
 *      Suppressed exiting parameter from the saveInfo function
 *      Fixed minor bugs in persistence management saeInfo/restore scheme
 * 6.4.1a
 *      Bug fixed: Problem in area change on colliding IDs
 * 6.4.0f
 *      Bug fixed: In case of problems during MoveOutside the world did not empty the queue
 *      Released as 6.4.1
 * 6.4.0e
 *      moveoutside code almost completely rewritten and optimized
 *      hideobject optimized and fixed bug about vanishing objects
 * 6.4.0d
 *      Enhancement: saveInfo(exiting) has been enhanced
 *      Bug fixed: The onSave event was not respecting the Devref specifications (save/save and exit). Fixed.
 *      Enhancement: word variables: instanceid and debugmode
 * 6.4.0c
 *      Bug fix: Default value in dropdown controls was always interpreted as an expression
 * 6.4.0b
 *      Changed default behaviour: when looking at a character, attributes are shown before the inventory
 *      Fixed visualisation problem on IE in presence of the right column when looking at characters
 * 6.4.0a
 *      Added function getObjectsSubtype
 * 6.3.6h
 *      Corrected bug in Copy() function
 *      Released as 6.4.0
 * 6.3.6g
 *      Added the Copy() function for cloning ARRAYs and SETs
 *      The ARRAYs are now implemented using the faster DictSortedc class
 *      Testrug1 updated for new tests on arrays and sets
 * 6.3.6f
 *      SetIndexOf is now NOT case sensitive
 * 6.3.6e
 *      Added function HttpFetch()
 *      default Skin now has icon names in all-lowercase characters
 * 6.3.6d
 *      Modified skin code to patch functionality of right_column style in IE6
 * 6.3.6c
 *      Sample CustCmdProcessor updated to implement IWorldInitProcessor interface.
 * 6.3.6b
 *      Added support for IWorldInitProcessors to extend pre- and post- init processing.
 * 6.3.6a
 *      Added support for custom command processors such as the ISayProcessor.
 *      Adds an optional 'custCmdProc' attribute to WORLD definition files.
 * 6.3.5d
 *      Bug fix: Dimx was outputting the wrong character encoding (thanks Przemek) - perhaps now fixed
 *      Released as 6.3.6
 * 6.3.5c
 *      Fixed issue with initial screen resolution
 *      Enhancement: New function: gameInfo("imagesfolder")
 *      Enhancement: New instruction: printRight, and right_column management
 * 6.3.5b
 *      Added cmd#26: Help in messages file
 * 6.3.5a
 *      Default resultion now set to 640x480 to encourage game embedding in sites
 *      Refined caching prevention method
 * 6.3.4e
 *      Enhancement: Added getPlayerProperties() function
 *      Released as 6.3.5
 * 6.3.4d
 *      Bug fix: Missing $OWNER object was producing the player to be used as such - fixed
 * 6.3.4c
 *      Bug Fix: Missing $TARGET parameter for custom command events - fixed
 * 6.3.4b
 *      Enhancement: expressions can now be used as event ids in PANEL controls specifications
 *      Modify in parsing expressions list as function arguments
 * 6.3.4a
 *      Fix: Disabled possibility to hide objects placed into other ojects
 * 6.3.3f
 *      Added a new object attribute: invisible
 *      Released as 6.3.4
 * 6.3.3e
 *      Fixed nullpointer in Client.java:196
 * 6.3.3d
 *      Fixed problem in messages related to looking a player
 *      Added variable .__clearinfo to disable properties printout in characters/items
 * 6.3.3c
 *      Fixed bug in getObjectsType: search for a null type was returning the first item in container - now returns null set
 * 6.3.3b
 *      PANEL Dowdown control: default value is now interpreted as an expression
 * 6.3.3a
 *      Adjustments for HTML correctness
 * 6.3.2c
 *      Fixed nullPointer in Player.java:241 and Player.java:647
 *      Added support for different maps fot the same world (ROOM.map attribute). client script changed.
 *      Released as 6.3.3
 * 6.3.2b
 *      Changed visualisation code for links having showFor images
 *      Optimization for Open&Close operations
 *      Added method: Player.getPanel()
 *      Enhancement: now displays map coordinates when in debug mode
 * 6.3.2a
 *      Added support for Access database via ODBC data source
 *      Added support for screen resolutions: 1280x800, 480x272 (PSP)
 * 6.3.1f
 *      Bug fixed: Maintenance panel didn't always check for password match
 *      Enhancement: Maintenance panel - Added game date-based profile selection
 *      Enhancement: Maintenance panel restyled
 *      Added messages file for Spanish
 *      Released as 6.3.2
 * 6.3.1e
 *      Bug fixed: Didn't catch the admin password
 * 6.3.1d
 *      Added Music explanation on logon screen
 *      Removed musicProtect setting
 *      Enhancement: Added msg14 and msg15 for music feature explanation
 * 6.3.1c
 *      Enhancement: Added NewArray() function
 *      Enhancement: Added NewLink() function
 * 6.3.1b
 *      Bug fixed: Number index for sets/arrays was not properly handled under certain circumstances
 * 6.3.1a
 *      Changed format for date in saveGame, "_when" setting
 * 6.3.0e
 *      Enhancement: ACCEPTS property now checks also main type
 *      Released as 6.3.1
 * 6.3.0d
 *      Bug Fix: Made Exists() function more robust
 *      Enhacement: Added getObject() function
 * 6.3.0c
 *      Bug Fix: Suspect NullPointer weakness in Image.metHtml()
 * 6.3.0b
 *      Enhancement: Added XSPF Flash player for MP3 files streamplay
 *      Enhancement: Added server setting: musicProtect
 *      Enhancement: New functions: UCase, gameInfo("site")
 *      Enhancement: PanelHtml() function - client.script modified
 *      Bug fix: Audio FX support check instead of Music support - pup up didn't open
 * 6.3.0a
 *      Enhancement: Added IMAGE.html() method
 * 6.2.4e
 *      Bug fix: NullPointer during logon (Cluster.java:125) in case of cluster config mistake
 *      Released as 6.3.0
 * 6.2.4d
 *      Enhancement: Now supports HOOKS directive
 * 6.2.4c
 *      Maint panel: Now lists all the really allocated slots
 *      Area change: On-screen error in case of problem during MoveOutside
 * 6.2.4b
 *      Fixed bug: Facing correction was not correct when original facing was West
 * 6.2.4a
 *      Solved caching problem in IE (should not cache)
 * 6.2.3d
 *      Enhancement: Added return value to onHear event to cancel action
 *      Enhancement: Added go("direction") method to character object
 *      Released as 6.2.4
 * 6.2.3c
 *      Fixed NullPointer on invalid use of a function
 * 6.2.3b
 *      Enhancement: introduced __clearfocus variable to cancel focus on objects
 *      Enhancement: Added return value to onLook event to cancel action
 * 6.2.3a
 *      Enhancement: onHear event now extended to all objects excluding $WORLD
 * 6.2.2i
 *      Bug fixed: NullPointer in multiplayer.service(multiplayer.java:1103)
 *      Bug fixed: Removed unused blank line in admin panel, command window
 *      Released as 6.2.3
 * 6.2.2h
 *      Solved NullPointer in AdvObject.isAccessibleFrom(AdvObject.java:196)
 * 6.2.2g
 *      Solved the facing issue - now in 3rd person view the player is always seen from the back
 *         More in general, all objects are assumed to have facing, and it is now handled correctly
 *      Objects are not focusized when no more accessible (marius' onLook issue)
 * 6.2.2f
 *      Added code for closing DBconnections and statements to the database
 * 6.2.2e
 *      Modified for establishing on/the fly connections.
 *      DB connection check added to Maint panel
 * 6.2.2d
 *      Bug fix: Fixed NullPointer in function: getCharactersIn(null)
 *      Bug fix: Fixed IllegalArgumentException in Player.SaveCookie
 * 6.2.2c
 *     Bug fix: Maintenance panel crashed when exporting DB of a game whose name contained spaces
 * 6.2.2b
 *     Added support for clusterlist
 *     Auto pull-up of configured cluster worlds via IFRAMEs
 *     More work on accessibily - now scene frame validates correctly too.
 * 6.2.2a
 *     Bug fixed: MoveOutside issued in onReceive event was producing a NullPointer exception
 * 6.2.1e
 *     Added player.getCookie(key) method
 *     Added player.saveCookie(key,value) method
 *     Enhancement: Cleaned up HTML code produced by engine, specified DOCTYPE, most frames will validate now
 *     Released as 6.2.2
 * 6.2.1d
 *     Enhancement: Added support for methods to the SmallBasic language interpreter
 *     Bug fixed: Sound settings retrieved from cookie were not properly represented on the connect panel
 * 6.2.1c
 *     Enhancement: Added "tracing" setting for any game slot: enables tracing on SQL database
 *     Changed main log line - now suitable for player tracing
 * 6.2.1b
 *     Bug fixed: HTML code produced for Day/Night cycles was not compatible with Firefox and Opera (thanks Marius)
 *     Muted warning in case "FOR"'s startval >= endval
 * 6.2.0g
 *     Bug fixed: Removed annoying error message from the log if no mysqlDB at startup
 *     Enhancement: Attempts to auto restore a broken DB connection each 5 minutes
 *     Bug fixed: SetRemove used inside a For..Each loop was producing a NullPointer
 *     Bug fixed: Capacity was not saved in player profile (now it is, if different than default one)
 *     Released as 6.2.1
 * 6.2.0f
 *     Bug fixed: login attempts failed when the session was expired and browser still open
 * 6.2.0e
 *     Bug Fixed: SaveExit was acting differently from Save command (thanks Gunn)
 * 6.2.0d
 *     Modified Utils.pageExpires for suspect caching bug
 * 6.2.0c
 *     Bug Fix: SetKeys returned unsorted array on long sets
 * 6.2.0b
 *     Bug Fix: Living event cycle - on multiple dying characters, some of the dying characters remained alive
 * 6.2.0a
 *     Fix: maint servlet - no more echo prints when exporting from DB
 * 6.1.8k
 *     Restored support for SWF/MOV files
 *     Released as 6.2.0
 * 6.1.8j
 *     DB Conn check and reset message made optional
 * 6.1.8i
 *     Bug fix: Saved profiles count was always calculated on file - fixed
 *     Bug fix: Maintenance/profiles removal did not include properties/events - fixed
 * 6.1.8h
 *     Enhancement: Auto-restore of the DB connection if it falls during game time
 *     Bug fix: Errors on DB were tolerated at login phase
 *     Bug fix: Useless loading of SAV file during saveSetting - corrected
 *     Bug fix: Connection check were producing errors on log for table names with spaces
 * 6.1.8g
 *     Bug fixes on DB setup upon startup
 * 6.1.8f
 *     Enhancement: Now tolerates missing DB conection upon starting
 * 6.1.8e
 *     Enhancement: SaveSetting/GetSetting now works transparently also on the DB
 * 6.1.8d
 *     Enhancement: Added support for MySql databases. Now if properly configured (see new db settings in dimensionex.properties) opens connection at startup
 * 6.1.8c
 *      Enhancement: Function getItemsType() modified so to fit the type.subtype model
 * 6.1.8b
 *      Bug fix: BannedIPs filter was working only for one-IP list - fixed
 * 6.1.8a
 *      Recomipled the source code (NoSuchMethod Exception when opening Maintenance page)
 *      Re-packaging with update messages files, and updated Underworld ENG game source
 * 6.1.7h
 *      Bug fix: Vehicle management in multi-face rooms was not working correctly
 *      Enhancement: Added showPolicyPeople attribute for people-containing objects and vehicles. set to 1 to have on-screen display of people
 *      Released as 6.1.8
 * 6.1.7g
 *      Enhancement: Added "connect" panel, for customising connect screen
 *      Enhancement: Added "DELETE" panel command, to remove unwanted controls
 * 6.1.7f
 *      Enhancement: Anti-offense: Muting completed - msg 147 added
 * 6.1.7e
 *      Bug fix: Fixed some issues in set reconstruction from profile DB.
 * 6.1.7d
 *      Enhancement: Anti-offense system
 * 6.1.7c
 *      Enhancement: The Replace() function is now case insensitive
 * 6.1.7b
 *      Enhancement: Added support for type.subtype scheme
 *      Enhancement: Added MainType() function for extracting main types
 *      Enhancement: Finished Admin "Enter Script" (formerly "add script") command
 *      Enhancement: Added Admin "Execute" command
 * 6.1.7a
 *      Enhancement: Removed server time and added smooth scene transitions
 *      Bug fix: Fixed bug for restore agem when saved in object that is being destroyed
 *      Bug fix: Sky positioning in 1024x768
 * 6.1.6f
 *      Bug fix: Background box was not properly scaled
 *      Released as 6.1.7
 * 6.1.6e
 *      Enhancement: Added support or scene background
 * 6.1.6d
 *      Bug fix: Modified code to handle NullPointer in multiplayer:1100 and 1095
 * 6.1.6c
 *      Bug fix: showFor property was not assignable at run-time
 * 6.1.6b
 *      Enhancement: Uses binary search in DictSorted objects for optimized speed
 * 6.1.6a
 *      Bug fixed: Disappearing item problem when passing from an area to another
 *      Enhancement: Optimizer now idexes by short filename:line instead of event id
 * 6.1.5e
 *      Bug fixed: Potential problem in PANEL definition if a CR was inserted before a TEXBOX
 *      Bug fixed: Reading from objects' showmode property yelding wrong result
 *      Released as 6.1.6
 * 6.1.5d
 *      Added consistency checks to avoid the ghost objects problem after area change
 * 6.1.5c
 *      Trying to fix disappearing objects bug during area change
 * 6.1.5b
 *      BugFix:NullPointer in Maint servlet when no world loaded
 * 6.1.5a
 *      Enhancement: Adding new admin commands
 * 6.1.4i
 *      Enhancement: Movement events activated for area change
 *      Released as 6.1.5
 * 6.1.4h
 *      Bug fix: Fixed nasty bug in sets management - Value was altered in read-only use
 *      Enhancement: Added Sqr() function for square root
 *      Enhancement: Added TEXTBOX [id] syntax for multiple textboxes
 *      Enhancement: Added "cluster=" parameter, "players" view with "extended" format
 * 6.1.4g
 *      Bug Fix: Further fix for area change of object containing other objects
 * 6.1.4f
 *      New destination URL for banned clients
 *      Bug fix: Fixed some hyperlinks in the Admin Snapshot view
 * 6.1.4c,d,e
 *      Fixed bug in management of vanishing objects during area change
 * 6.1.4b
 *      Big Fix: ID of player was not correctly fixed up during area change
 * 6.1.4a
 *      DimX error when assigning to object.container turned into warning on the log
 *      Enhancement: added Ban instruction
 *      Enhancement: added Bannning system!
 *      Enhancement: added system variable _bannedClients (pipe-separated string of banned IPs)
 * 6.1.3g
 *      Bug Fix: Resolved NullPointer when trying to access input() set in standard event
 *      Enhancement: input() set values now passed also for "look" command
 *      Enhancement: Added IsItem() function
 *      Released as 6.1.4
 * 6.1.3f
 *      Enhancement: Added InStr() Function
 *      Enhancement: Trims spaces of nicknames at logon
 *      Enhancement: Added getObjectsType function
 *      Enhancement: null location now allowed in NewItem function
 * 6.1.3e
 *      Added cluster-wide nickname check
 * 6.1.3d
 *      Fixed bug in player.saveGame affecting save of Image objects
 *      Fixed bug in multiplayer class, NullPointer exception when player.container = null
 *      Added throw of DimXException when getting reference of a manageable object is tried (infinite loop in Ransagor game)
 * 6.1.3c
 *      Fixed bug in Player.restoreProperties
 * 6.1.3b
 *      More synchronization to save game database: saveSetting
 * 6.1.3a
 *      Patched Player.restoreProperties to tolerate errors in Image restore
 * 6.1.2o
 *      Bug fix: Fixes to the Save/restore operation on SET objects into the game profiles database
 *      Released as 6.1.3
 * 6.1.2n
 *      Enhancement: Added Player.remoteAddr property for IP blocking
 *      Bug fix: prevented error during hide of vanishing objects
 * 6.1.2m
 *      Bug Fix: Save Game procedure revised. Should be more robust and optimized now, and supports saving of SETS, event sets of images
 *      Bug fix: Added semaphore to avoid problems in concurrent use of SaveGame on same cluster worlds
 *      Enhancement: Now supprots Print of images
 * 6.1.2k
 *      Bug fix: Prevented NullPointer exception in Player.saveGame when Player is in NULL
 * 6.1.2j
 *      Bug Fix: Exception trying Restore from saved game when player had saved game when placed in dynamic object
 *      Bug fix: Exception trying to Speak when in NULL
 *      Released as: 6.1.3
 * 6.1.2i
 *      Bug Fix: Name of the room in the scene bar was disappeared
 *      Bug Fix: Removed exception throw in AdvObject.die() - it was preventing disposal of Player objects
 * 6.1.2h
 *      Bug fix: Issues during management of player without any images
 *      Enhancement: Added binary operator: power elevation (^)
 *      Enhancement: Added support for 3RD person look. GUI tag: SCENE LOOK 3RDPERSON/1STPERSON
 *      Bug fix: Problem in area change when using default panel
 * 6.1.2g
 *      Bug Fix: Vehicles / Open items
 *      Bug Fix: Assignment to a un-existent SET element for adding new elements
 *      Bug Fix: SetKeys() function was not working properly - fixed
 *      Enhancement: INTERPHONE level now to 2
 *      Optimization in AdvObject
 * 6.1.2f
 *      Found bug in vehicles management
 *      Fixed exception management in moveOutisideNow
 * 6.1.2d
 *      Bug Fix: Lost command panel after area change
 * 6.1.2c
 *      Bug Fix: NullPointer when referencing a un-existent SET element
 * 6.1.2b
 *      Bug Fix: Prevented NullPointer in multiplayer.java:881
 * 6.1.2a
 *      Bug Fix: Fixes to the AutoRestore logic - it was reporting restore OK when AutoRestore did not succeed
 *      Enhancement: Nickname check; New world setting: stopwords to define forbidden words and characters
 * 6.1.1x
 *      Added getRooms() function
 *      Released as 6.1.2
 * 6.1.1w
 *      Optimization in AdvObject.objRemove
 * 6.1.1v
 *      Another patch: NullPointer in objRemove for un-existent object
 *      NullPointer in multiplayer.service (thisPlayer)
 * 6.1.1t
 *      Another patch NullPointer htmlView
 *      Problem in objRemove for un-existent object
 * 6.1.1s
 *      Patched again: NullPointer in htmlView (focus)
 *      Problem in move of null world object
 * 6.1.1r
 *      Bug fixed: NullPointer when using unary minus (-)
 * 6.1.1q
 *      Bug fixed: More fixes for correct management of area change
 *      Bug fixed: AdvObject.go ERROR log message suppressed
 * 6.1.1p
 *      Tuned-up management of objects (Token)
 *      Bug Fixed: When changing area sometimes the player was drop off
 * 6.1.1o
 *      Bug fix: Fixed area transfer for nexted objects
 *      Bug fix: Reference to external areas are now protected
 * 6.1.1n
 *      Enhancement: AutoRestore can be now disabled
 *      Bug fixed: SendPage was not working ("Type mismatch: VIEW object expected")- fixed
 * 6.1.1m
 *      Bug fixed: removal of objects in moveOutside was jumping over items due to bad loop management
 * 6.1.1k
 *      Checked removal of dead object
 * 6.1.1j
 *      Bug Fix: NullPointer in multiplayer.htmlProperties:1786
 *      Bug Fix: No image in multiplayer.htmlScreenDisplay:1674
 *      Bug fix: Non-existent object in AdvObject.hide, Item.restoreTo
 * 6.1.1i
 *      Bug fix: NullPointer in htmlView:1962
 * 6.1.1h
 *      Bug fix: NullPointer in getImageAndCorrectFacing:1203
 * 6.1.1g
 *      Bug fix: NULL target in whenPicked event
 * 6.1.1f
 *      Bug fix: Nullpointer for null image in AdvObject.hide:153
 *      Bug fix: Bad exception logging in Player.saveGame()
 *      Bug fix: Null command in RefreshView
 * 6.1.1e
 *      Bug fix: Nullpointer for null image in multiplayer:1659
 *      Bug fix: Nullpointer for null world in objRemove:688
 * 6.1.1d
 *      Bug fix: NullPointer in Player.look() line 126
 *      Enhancement: Now logs on file general exception caught in multiplayer.service
 * 6.1.1c
 *      Tried to fix NullPointer in Player.look() line 126
 * 6.1.1b
 *      Bug Fixed: Recent changes affected inter-dimensional jumps (moveOutside)
 * 6.1.1a
 *      Added file logging of server errors
 *      Added check for NULL world in Event.fire
 * 6.1.0d
 *      Fix: onLook() event result now is printed on top of player info
 *      Enhancement: new setting: hideSourcePath for source protection
 *      Released as 6.1.1
 * 6.1.0c
 *      Enhancement: AutoRestore now working fine
 *      Enhancement: Added LCase function
 * 6.1.0b
 *      Enhancement: first version of AutoRestore
 * 6.1.0a
 *      Enhancement: supports events object.saveInfo for restoreinfo processing
 *      Enhancement: now stores type and restoreinfo in items
 *      Enhancement: now starts "restore" Event upon item restore
 * 6.0.2d
 *      Enhancement: New MAP control for custom panels
 *      Enhancement: Now stops world loading upon syntax error in skin file
 *      Enhancement: DXS syntax now supports a new STYLESHEET tag for extra custom settings on the UI
 *      Released as 6.1.0
 * 6.0.2c
 *      Enhancement: New function: Chr()
 *      Bug fix: Previous versions did not consider semicolon ";" as punctuation - fixed
 *      Bug fix: Previous versions did not alert upon system function redefinition - fixed
 * 6.0.2b
 *      Bug Fixed: Bug when restoring incomplete profile
 *      Fixed: Image tooltips for Firefox Browsers
 * 6.0.2a
 *      Bug Fixed: NullPointer in getSetting
 * 6.0.1c
 *      For..Next now tolerates first index > end index
 *      SetLen now returns -1 in case of wrong argument instead throwing error
 *      Added function: NewSet
 *      Added function: SetKey
 *      Undocumented Keys function renamed to SetKeys and documented
 *      Added function: Replace
 *      Released as 6.0.2
 * 6.0.1b
 *      Enhancement: saveSetting of SETS
 *      Finished support for drop-down list
 *      added optional parameters to getSetting function
 * 6.0.1a
 *      Enforced Player.saveGame
 *      Corrected bug in MAP management when changing PANEL
 * 6.0.0c
 *      Enhancement: Added "effect" property to ROOM objects
 *      Fixed bug: Hide all command was not always working
 *      Released as 6.0.1
 * 6.0.0b
 *      Fixed rare bug in cluster management
 * 6.0.0a
 *      Room description gets now printed anyway
 *      Added debugging code to Actionsrunner.actSendCmd
 * @author Cristiano Leoni
 */
public class multiplayer extends javax.servlet.http.HttpServlet {
    // Shown version
    public static String myVersion = "7.1.4";
    public String navigatorUrl = "/dimx/servlet/cleoni.adv.multiplayer";
    private DictSorted worlds = null;
    private DictSorted clusters = null;
    private DictSorted clusterslist = new DictSorted();
    
    // Vars
    private  int count=0; // for banners
    
    public String dbdriver = "";
    private String dbhost = "";
    private String dbport = "";
    private String dbname = "dimensionex";
    private String dbuser = "root";
    private String dbpass = "";
    public String dbconnstr = "";
    
    // Default Settings
    public  Object serverlock = new Object(); // Synchronisation tool
    
    private  String serverType = "public";
    private  String serverAdminPasswd = "";
    private  int msgRefreshRate = 5;

    
    // Static Configuration
    private  String systemDir = null; // InitParameter "base"
    private  String savingDir = null; // NULL will mean: "save into systemDir"
    //private  Messages msgs;
    private static java.util.Random rndGen = new java.util.Random();
    
    public World getWorld(String areaid) {
        return (World) worlds.get(getSlot(areaid));
    }
    
    private String getSlot(String areaid) {
        for (int i=0; i < worlds.size(); i++) {
            World w1 = (World) worlds.elementAt(i);
            if (w1.id.equalsIgnoreCase(areaid)) {
                return worlds.keyAt(i);
            }
        }
        return null;
    }
    
    public Cluster getCluster(String clusterid) {
        return (Cluster) clusters.get(clusterid);
    }
/*
 *
 * Reads incoming command and
 * identifies client
 *
 *
 */
    private Dict getCommand(String charset, Utils utils) {
        Dict v = new Dict(6,1,false);
        
        //Read command if explicit and normalize
        String cmd = utils.getForm("cmd",charset);
        if (cmd.indexOf("\n") > 0) { // Strip newline
            cmd = cmd.substring(0,cmd.indexOf("\n")-1);
        }
        
        // Try to read command from a SUBMIT button
        if (!utils.getForm("login",charset).equals("")) {
            cmd = "login";
        } else if (!utils.getForm("return",charset).equals("")) {
            cmd = "return";
        } else if (!utils.getForm("startscratch",charset).equals("")) {
            cmd = "startscratch";
        }
        String txtBox = utils.getForm("txtBox",charset);
        if (cmd.equals("") && !txtBox.equals("")) {
            // Patch for quick Chat
            v.put("cmd","say");
            v.put("arg1",txtBox);
            v.put("arg2","*");
        } else {
            v.put("cmd",cmd);
            v.put("arg1",utils.getForm("arg0",charset));
            v.put("arg2",utils.getForm("arg1",charset));
        }
        Dict params = utils.getForm();
        v.put("input", params);
        return v;
    }
    
    public boolean isDBConfigured() {
        return (!dbconnstr.equals("") && (!dbdriver.equals("")));
    }
    
/*
 *	Load settings
 */
    public DictSorted loadWorldSettings(String slot,String propFile) throws DimxException {
        String tmp;
        DictSorted settings = new DictSorted();
        
        java.util.Properties p = new java.util.Properties();
        try {
            p.load(new java.io.FileInputStream(propFile));
            //then get properties as follows
            settings.put("worldFile","world.dxw");
            tmp = Utils.cStr(p.getProperty("worldFile"));
            if (!tmp.equals("")) {
                settings.put("worldFile",tmp);
            }
            
            settings.put("debugMode",null);
            tmp = Utils.cStr(p.getProperty("debugMode"));
            if (Utils.cInt(tmp)>0) {
                settings.put("debugMode","1");
            }
            
            settings.put("disableAutoRestore",null);
            tmp = Utils.cStr(p.getProperty("disableAutoRestore"));
            if (Utils.cInt(tmp)>0) {
                settings.put("disableAutoRestore","1");
            }
            
            settings.put("tracing",null);
            tmp = Utils.cStr(p.getProperty("tracing"));
            if (Utils.cInt(tmp)>0) {
                settings.put("tracing","1");
            }
            
            settings.put("hideSourcePath",null);
            tmp = Utils.cStr(p.getProperty("hideSourcePath"));
            if (Utils.cInt(tmp)>0) {
                settings.put("hideSourcePath","1");
            }
            
            settings.put("clientFile",systemDir + "standard.client");
            tmp = Utils.cStr(p.getProperty("clientFile"));
            if (!tmp.equals("")) {
                settings.put("clientFile",systemDir + tmp);
            }
            Logger.echo("clientFile in use: "+ settings.getS("clientFile"));
            
            settings.put("clientScript","client.script");
            tmp = Utils.cStr(p.getProperty("clientScript"));
            if (!tmp.equals("")) {
                settings.put("clientScript",systemDir + tmp);
            }
            Logger.echo("clientScript in use: "+ settings.getS("clientScript"));
            
            tmp = Utils.cStr(p.getProperty("tickLength"));
            int tmpTickLength = Utils.cInt(tmp);
            if (tmpTickLength>0) {
                if ((tmpTickLength>=5) && (tmpTickLength<=60)) {
                    settings.put("tickLength",tmpTickLength);
                    Logger.echo("tickLength: "+tmp);
                } else {
                    Logger.echo("invalid tickLength: "+tmp);
                }            
            }
            
            tmp = Utils.cStr(p.getProperty("msgsFile"));
            if (!tmp.equals("")) {
                settings.put("msgsFile",systemDir + tmp);
            }

            tmp = Utils.cStr(p.getProperty("journal"));
            if (!tmp.equals("")) {
                settings.put("journal",tmp);
            }

            tmp = Utils.cStr(p.getProperty("analytics"));
            if (!tmp.equals("")) {
                settings.put("analytics",tmp);
            }

            String logname = systemDir + "debug" + slot + ".log";
            tmp = Utils.cStr(p.getProperty("debugTo"));
            if (tmp.equalsIgnoreCase("file")) {
                settings.put("logger",new Logger(logname,0));
            } else if (tmp.equalsIgnoreCase("none")) { // no debug at all
                settings.put("logger",new Logger(logname,2));
            } else { // debug to console
                Logger.echo("Log messages will be sent to console");
                settings.put("logger",new Logger(logname,1));
            }
            
            settings.put("stopwords","<,>,\"");
            tmp = "<,>,\""+Utils.cStr(p.getProperty("stopwords"));
            if (!tmp.equals("")) {
                settings.put("stopwords", tmp);
            }
            
            settings.put("adminPasswd",Utils.cStr(p.getProperty("adminPasswd")));
            
            //Logger.echo("Settings for slot " + slot + " have been loaded");
            return settings;
        } catch (Exception e) {
            throw new DimxException("Cannot load settings: " + e.getMessage() + "\nEither the servlet settings are wrong, or the configuration file was deleted, moved or renamed");
        }
    }
    public void loadSettings(String propFile) throws DimxException {
        String tmp;
        
        java.util.Properties p = new java.util.Properties();
        try {
            p.load(new java.io.FileInputStream(propFile));
            
            tmp = Utils.cStr(p.getProperty("navigatorUrl"));
            if (!tmp.equals("")) {
                navigatorUrl = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("serverType"));
            if (tmp.equalsIgnoreCase("public") || tmp.equalsIgnoreCase("local")) {
                serverType = tmp;
            }

            tmp = Utils.cStr(p.getProperty("savingDir"));
            if (!tmp.equals("")) {
                savingDir = tmp;
            }

            tmp = Utils.cStr(p.getProperty("msgRefreshRate"));
            if (!tmp.equals("")) {
                msgRefreshRate = Utils.cInt(tmp);
            }
            
            tmp = Utils.cStr(p.getProperty("adminPasswd"));
            if (!tmp.equals("")) {
                serverAdminPasswd = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("dbdriver"));
            if (!tmp.equals("")) {
                dbdriver = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("dbhost"));
            if (!tmp.equals("")) {
                dbhost = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("dbport"));
            if (!tmp.equals("")) {
                dbport = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("dbname"));
            if (!tmp.equals("")) {
                dbname = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("dbuser"));
            if (!tmp.equals("")) {
                dbuser = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("dbpass"));
            if (!tmp.equals("")) {
                dbpass = tmp;
            }
            
            if (!dbhost.equals("") && !dbuser.equals("") && !dbname.equals("")) {
                if (dbport.equals(""))
                    dbconnstr = "jdbc:mysql://" + dbhost + "/" + dbname + "?user=" + dbuser + "&password=" + dbpass;
                else
                    dbconnstr = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname + "?user=" + dbuser + "&password=" + dbpass;
            }
            
            tmp = Utils.cStr(p.getProperty("dbconnstr"));
            if (!tmp.equals("")) {
                dbconnstr = tmp;
            }
            
            tmp = Utils.cStr(p.getProperty("clusters"));
            if (!tmp.equals("")) {
                clusterslist = makeClustersList(tmp);
            }

            Logger.echo("Server Settings loaded.");
        } catch (Exception e) {
            throw new DimxException("Cannot load server settings: " + e.getMessage() + "\nEither the servlet settings are wrong, or the configuration file was deleted, moved or renamed");
        }
    }
/*
 *
 *	Carica World
 *
 */
    private World loadWorld(String slot, String worldFile, String msgsFile, Logger logger) throws DimxException, NestedException {
        World world = null;
        boolean doDefProc = true;
        try {
            WorldLoader loader = new WorldLoader(logger,new Messages(msgsFile));
            world = loader.load(this,systemDir,worldFile,serverType);
            
            if (world != null) {
                world.slot = slot;
                if (world.defaultCharacter == null) {
                    // System Character Is Missing - add default one
                    Character newCh = new Character(world,world.msgs.msg[11],"SYS","",null,9,world.charactersDefaultAttrlist,null,Const.ACCEPT_NOTHING);
                    world.addCharacter(newCh);
                    world.defaultCharacter = newCh;
                }
                world.logger.debug("Loaded wordfile.");
                
                // check for custom IWorldInitProcessor.preInit
                if( world.custCmdProc instanceof IWorldInitProcessor){
                    world.logger.debug("Custom pre-INIT processor invoked...");
                    doDefProc = ((IWorldInitProcessor)world.custCmdProc).processPreWorldInit(this, loader, world, systemDir);
                }else{
                    doDefProc = true;
                }
                
                // default init
                if (doDefProc) {
                    world.init();
                }
                
                // check for custom IWorldInitProcessor.postInit
                if( world.custCmdProc instanceof IWorldInitProcessor){
                    world.logger.debug("Custom post-INIT processor invoked...");
                    doDefProc = ((IWorldInitProcessor)world.custCmdProc).processPostWorldInit(this, loader, world, systemDir);
                }else{
                    doDefProc = true;
                }
                
                // default postInit
                if (doDefProc) {
                    world.switchEvents(true);
                    // For world, systemDir is actually the savingDir
                    if (savingDir == null) { // Standard - use systemDir for saving
                        world.systemDir = systemDir;
                    } else { // Use alternate saving location
                        world.systemDir = savingDir;
                    }
                }
                
            } else {
                throw new DimxException("Unable to load worldfile: " + worldFile);
            }
        } catch (NestedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new DimxException(e.getMessage());
        }
        return world;
    }
    
    
    
    
/*
 *
 *	MAIN LOOP
 *
 */
    /** Main loop for the DimensioneX game engine
     * @param request standard
     * @param response standard
     * @throws IOException standard
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        
        Utils utils = new Utils(request,response);
        navigatorUrl = request.getRequestURI();
        
        // Try to get slot ID from the URL
        String slot = utils.getFormSession("game");
        String on_cluster = utils.getFormSession("cluster");
        
        World world = null;
        // Define charset for output
        String charset = "ISO-8859-1";
        boolean specialmode = false;
        PrintWriter out = null;
        
        if (slot.equals("")) {
            slot = "1";
        }
        
        try {
            if (worlds == null) initServer();
            
            if (!slot.equals("")) {
                if (!utils.getForm("view").equals("players"))
                    utils.setSession("game", slot);
                world = (World) worlds.get(slot);
                if (world == null)
                    synchronized (serverlock) { // CRITICAL
                    world = initSlot(slot);
                    }
            } else {
                response.setContentType("text/html");
                utils.pageExpires();
                out = response.getWriter();
                out.println("<HTML><BODY>Please specify a game slot by appending to the server URL: ?game=slot</BODY></HTML>");
                return;
            }
            
            if (world != null) {
                charset = world.msgs.charset;
                specialmode = world.msgs.specialmode;
            }
            
            // Set headers
            if (specialmode) {
                // Works for Russian (windows-1251) but not for others. Seems to block "View HTML"
                // Probably because it differs from the meta HTTP equiv output by World.htmlCharset
                response.setContentType("text/html");
            } else {
                // Works for all charsets excluding Russian (windows-1251)
                response.setContentType("text/html; charset=" + charset);
            }
            out = response.getWriter();
            response.setHeader("P3P", "CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");
            utils.pageExpires();
            
            // This worked for Chinese but not for other languages
            // suggested by Marcus Chan
            //PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(),charset), true);
            // This seems to works for all and seems to work quicker
            //PrintWriter out = response.getWriter();
            
            // Read input
            Dict kv = getCommand(world.msgs.charset,utils);
            String cmd = Utils.cStr(kv.getS("cmd"));
            String view = utils.getForm("view",charset);
            
            if (world != null) {
                //*************************************
                // MAIN LOOP
                
                world.logger.debug("-------- SERVICE - slot: " + slot + " - view: " + view + " - " + Utils.now());
                String console = "";
                String rightcolumn = "";
                StringBuffer msgError = new StringBuffer("");
                Vector sayProcessorResult = null;
                boolean doDefProc = true;
                
                // More useful vars
                Player thisPlayer = null;
                String userid = "";
                String usernick = "";
                Client msgb=null;	// Player's client
                String status = null;
                String msgRefreshUrl = "";
                
                // per Normal View
                AdvObject thisRoom = null; // player's current container
                String thisRoomDescription = null; // thisRoom's description
                Dict peopleV = null; // current People list
                Dict itemsV = null; // items list
                Dict msgV = null; // current Message list
                String commands = null; // current commands list
                Dict invV = null; // inventory
                String sound = null; // rimane tale a meno che view=msgs
                
                
                // per Message Popup
                Message thisMsg = null;	// current Message
                
                // Ban control
                if (!world.isValidIP(request.getRemoteAddr())) { // Redirect to a ban page
                    response.sendRedirect("http://dimensionex.sourceforge.net/banning.htm");
                    return;
                }
                
                if (view.equals("players") && !on_cluster.equals("")) {
                    if (clusters.get(on_cluster) == null) {
                        out.println("--LOAD cluster-");
                    }
                    if (clusters.get(on_cluster) != null) {
                        ViewCluster.outPlayers(world,out,world.getSkin(""),on_cluster,(Cluster) clusters.get(on_cluster), utils.getForm("format",charset));
                    } else {
                        throw new DimxException("Unable to load specified cluster");
                    }
                    return;
                }
                
                if (view.equals("")) {
                    // need just the client
                    utils.setSession("screenmode", utils.getForm("width",charset) + "x" + utils.getForm("height",charset));
                    utils.setSession("locked", utils.getForm("locked",charset));
                    String nick;
                    nick=utils.getForm("nickname",charset);
                    if (!nick.equals("") && !nick.equals("?")) {
                        utils.setSession("nickname", nick);
                    }
                    String clientFile=utils.getForm("client",charset);
                    if (((Utils.instrCount(clientFile, '/') > 0) || (Utils.instrCount(clientFile, '\\') > 0)) || (!(Utils.instrRev(clientFile,".client",true)>0))) {
                        clientFile=null; // Invalid client
                    }
                    if (clientFile!=null) {
                        clientFile=systemDir+clientFile;
                        utils.setSession("client",clientFile);
                    }
                    
                    if (cmd.equals("")) {
                        sendClient(world, out, clientFile);
                        return;
                    }
                }
                
                
                synchronized (world.lock) { // CRITICAL
                    
                    String tid=utils.getForm("tid",charset); // Used for ajax calls
                    if (tid=="") {
                        world.tick();
                    } else {
                        world.logger.debug("Ajax call ("+cmd+"), tick skipped");
                    }
                    if (world.reset) { // Flag check predisposto
                        world.logger.debug("performing RESTART upon world reset flag");
                        world = initSlot(slot);
                    }
                    count++;
                    
                    if (!view.equals("players")) {
                        
                        // Check on users' world
                        
                        String worldinstanceid = utils.gSession("worldinstanceid");
                        //logger.debug("User is coming from:" + worldinstanceid + " current is:" + world.instanceid);
                        
                        // If (command present or scene view) and (user has a world id different than the current) and (slot is the same ie. he has not just changed area)...
                        if ((!cmd.equals("") || view.equals("scene")) && !worldinstanceid.equals("") && !worldinstanceid.equals(world.instanceid) && slot.equals(utils.getFormSession("game"))) {
                            // New user - provide world and reset id
                            world.logger.debug("User coming from world " + worldinstanceid + " - resetting his session");
                            utils.setSession("worldinstanceid",world.instanceid);
                            utils.setSession("userid","");
                            utils.setSession("usernick","");
                            setStatus(world, utils, "DISCONNECTED");
                        } else { // Existing user - identify
                            userid = utils.gSession("userid");
                            usernick = utils.gSession("usernick");
                        }
                        
                        if (!userid.equals("")) { // User identified
                            thisPlayer = (Player) world.getPeople(userid); // Warning this may get robots!
                            if (thisPlayer == null) {
                                world.logger.debug("User: " + userid + " not found in current world: " + world);
                                // Manage Just-Changed-Area Case
                                World currWorld = (World) worlds.get(utils.getFormSession("game"));
                                if (world != currWorld) {
                                    world.logger.debug("Just changed area: " + userid + " no more in " + world + " now in " + currWorld);
                                    world = currWorld;
                                    slot = utils.getFormSession("game");
                                    thisPlayer = (Player) world.getPeople(userid);
                                }
                            }
                            if (thisPlayer != null && !thisPlayer.name.strVal().equals(usernick)) {
                                thisPlayer = null;
                                userid = ""; // World reset in the meantime
                                utils.setSession("worldinstanceid",world.instanceid);
                                utils.setSession("userid","");
                                setStatus(world,utils,"DISCONNECTED");
                            }
                        }
                        status = utils.gSession("status");
                        if (status == null || status.equals("")) status = "DISCONNECTED";
                        
                        if (world.tracing || world.logger.on) {
                            String room = "";
                            String nickFromCookie = utils.getCookie("nickname");
                            String args = kv.getS("arg1") + "," + kv.getS("arg2");
                            if (thisPlayer != null && thisPlayer.container != null) {
                                room = thisPlayer.container.name.strVal() + " (" + thisPlayer.container.id + ")";
                            }
                            if (world.logger.on) {
                                world.logger.debug("USER: " + nickFromCookie + " (" + userid + ") | ROOM: " + room + " | STATUS: " + status + " | COMMAND: " + cmd+ "(" + args + ") | VIEW: " + view + " | DATE: " + utils.now("yyyy-MM-dd HH:mm:ss") );
                            }
                            if (world.tracing) {
                                Cluster mycluster = world.getCluster();
                                java.sql.Connection dbc = null;
                                if (mycluster.dbConnected) dbc = mycluster.dbConn(null);
                                if (dbc != null) { // Normal save
                                    String table = "trace_" + world.cluster;
                                    room = Utils.stringReplace(room,"'", "''",false);
                                    nickFromCookie = Utils.stringReplace(nickFromCookie,"'", "''",false);
                                    args = Utils.stringReplace(args,"'", "''",false);
                                    String sql = "INSERT INTO `" + table + "` (user,nickname,room,status,command,args,view,date_contact) VALUES ('" +
                                            userid + "','" + nickFromCookie + "','" + room + "','" + status + "','" +
                                            cmd + "','" +  args + "','" + view + "','" + utils.now("yyyy-MM-dd HH:mm:ss") + "')";
                                    if (!utils.executeSQLCommand(sql, dbc,true, true)) {
                                        world.logger.log("Could not trace on DB. Tracing disabled. SQL: " + sql);
                                        world.tracing = false;
                                    }
                                    dbc.close();
                                }
                            }
                        }
                        
                        //
                        // Process command
                        //
                        if (cmd.equals("") || Utils.isIn(cmd,Const.offlineCmds) || Utils.isIn(cmd,world.hooks)) {
                            //
                            // if NO command or DISCONN commands
                            //
                            if (cmd.equals("login") || cmd.equals("startscratch")) {
                                // Initialize login parameters by input stream
                                String username = utils.getForm("username",charset).trim();
                                String skinid = utils.getForm("skin",charset);
                                String audiosup = (utils.getForm("audio",charset).equals("")?"0":"1")
                                + (utils.getForm("music",charset).equals("")?"0":"1");
                                String pass = utils.getForm("pass",charset).trim();
                                Dict myProfile = null;
                                
                                boolean ok_logon = false; // Flag
                                boolean valid = true; // Flag
                                
                                if (status.equals("DISCONNECTED") || status.equals("CONNECTED")) {
                                    // First stop
                                    if ((username.length() < 3) || (huntForbiddenChars(username) != null)) { // Short username or forbiden chars
                                        if ((myProfile = world.gameLoad(username)) == null) { // No saved profile
                                            valid = false;
                                        }
                                    }
                                    if (valid && (username.length() < 25) && (world.huntStopwords(username) == null) && !(username.equals("") || username.equals("?") || ((Cluster) clusters.get(world.cluster)).playerExists(username))) {
                                        // Valid username
                                        // Store values in users' cookies
                                        String bancontrol = utils.getCookie("control");
                                        if (bancontrol != null && bancontrol.equals("1")) {
                                            if (world.isValidIP(request.getRemoteAddr()) && !request.getRemoteAddr().equals("127.0.0.1")) {
                                                // Banned user - add IP to the list;
                                                world.banIp(request.getRemoteAddr());
                                            }
                                        }
                                        utils.setCookie("nickname",Utils.encodeURL(username));
                                        utils.setCookie("skin",skinid);
                                        utils.setCookie("sounds",audiosup);
                                        utils.setSession("screenmode",utils.getForm("screensize",charset));
                                        utils.setCookie("screenmode",utils.getForm("screensize",charset));
                                        if ((myProfile = world.gameLoad(username)) == null) {
                                            // No game saved
                                            ok_logon = true;
                                            myProfile = new Dict();
                                        } else {
                                            // Game saved
                                            // Store ref to profile in Session for later password check (second stop)
                                            status = "CONNECTING";
                                            utils.setSession("profile",myProfile);
                                            
                                        }
                                        myProfile.put("name",username);
                                        myProfile.put("skin",skinid);
                                        myProfile.put("audio",audiosup);
                                        
                                        Vector dim = Utils.stringSplit(utils.getForm("screensize",charset),"x");
                                        if (dim.size() == 2) {
                                            myProfile.put("screenwidth", dim.elementAt(0));
                                            myProfile.put("screenheight", dim.elementAt(1));
                                        }
                                    } else {
                                        valid=false;
                                    }
                                    if (!valid) {
                                        // Invalid Username
                                        msgError.append(world.msgs.msg[6] + "\n");
                                    }
                                } else if (status.equals("CONNECTING")) {
                                    // Second stop
                                    myProfile = (Dict) utils.getSession("profile");
                                    username = myProfile.getS("name");
                                    if (cmd.equals("login")) {
                                        // Check password as present in the profile
                                        if (myProfile != null) {
                                            // Valid profile
                                            if (!(username.equals("") || world.playerExists(username))) {
                                                // Valid username
                                                if (pass.equals(myProfile.get("pass")) || (world.debugging && !world.passchecked)) {
                                                    // Password match - OK restore profile
                                                    ok_logon = true;
                                                    world.passchecked = true; // Avoids bouncing
                                                    
                                                    // Persistency check
                                                    boolean must_restart = false;
                                                    if (world.savegamePersistence < 2 && !world.version.equals(myProfile.get("worldVersion"))) {
                                                        msgError.append("Game was updated since your last logoff.<BR>Your saved game cannot be restored, sorry.");
                                                        //msgError.append("'" + myProfile.get("worldVersion") + "'<>'" + world.version +"'");
                                                        must_restart = true;
                                                    } else
                                                        if (world.savegamePersistence < 1 && !world.instanceid.equals(myProfile.get("worldInstanceId"))) {
                                                        msgError.append("Game was restarted since your last logoff.<BR>Your saved game cannot be restored, sorry.");
                                                        must_restart = true;
                                                        }
                                                    
                                                    if (must_restart) {
                                                        myProfile.put("location",null);
                                                        myProfile.put("properties",null);
                                                        myProfile.put("events","");
                                                        myProfile.put("items",null);
                                                        myProfile.put("panel",null);
                                                        myProfile.put("image",null);
                                                    }
                                                } else {
                                                    // Wrong password
                                                    msgError.append(world.msgs.msg[123]);
                                                }
                                            } else {
                                                // Invalid Username
                                                msgError.append(world.msgs.msg[6] + "\n");
                                            }
                                        } else {
                                            // Lost profile
                                            msgError.append("Lost profile in session\n");
                                        }
                                    } else {
                                        // Start from scratch was chosen -
                                        // Ignore existing profile
                                        if (myProfile != null) { // Valid profile
                                            if (!(username.equals("") || world.playerExists(username))) {
                                                // Valid username
                                                if (pass.equals(myProfile.get("pass")) || world.debugging) {
                                                    // Password match - OK restore profile
                                                    myProfile.put("location",null);
                                                    myProfile.put("properties",null);
                                                    myProfile.put("events","");
                                                    myProfile.put("items",null);
                                                    myProfile.put("panel",null);
                                                    myProfile.put("image",null);
                                                    myProfile.put("worldId",world.id);
                                                    ok_logon = true;
                                                } else {
                                                    // Wrong password
                                                    msgError.append(world.msgs.msg[123]);
                                                }
                                                
                                            } // If valid username
                                        } // If valid profile
                                    } // If START FROM SCRATCH chosen
                                } // If status = connecting
                                
                                if (ok_logon) {
                                    // Checking area is OK
                                    String cluster = myProfile.getS("cluster");
                                    String areaid = myProfile.getS("worldId");
                                    if (cluster != null && !cluster.equals("") && areaid != null && !areaid.equals(world.id)) {
                                        world.logger.debug("User is coming from another area: " + myProfile.getS("worldId"));
                                        String toslot = getSlot(areaid);
                                        if (toslot != null) {
                                            String url = navigatorUrl + "?game=" + toslot+"&cmd=login&view=ctrls&pass=" + pass;
                                            World toarea = (World) worlds.get(toslot);
                                            utils.setSession("worldinstanceid",toarea.instanceid);
                                            utils.setSession("worldid",toarea.id);
                                            utils.setSession("game",toslot);
                                            //response.addHeader(response.SC_FOLLOW_REDIRECT_IN_ADDRESS_BAR, "true");
                                            response.sendRedirect(url);
                                            return;
                                        } else {
                                            world.logger.debug("Problem: target area: " + areaid + " is not currently active.");
                                            status = "DISCONNECTED";
                                            throw new DimxException("Target area: " + areaid + " is not currently active.\n");
                                        }
                                    }
                                    world.logger.debug("Creating client...");
                                    Client client = new Client(world,world.msgListSize,myProfile.getS("audio").charAt(0) == '1',myProfile.getS("audio").charAt(1) == '1',Utils.cInt(myProfile.getS("screenwidth")),Utils.cInt(myProfile.getS("screenheight")),request.getHeader("User-Agent") );
                                    client.session = utils;
                                    //TO DO:
                                    //client.clientFile should be set here, otherwise it will remain default standard.client
                                    String clientFile=utils.cStr(utils.getSession("client"));
                                    if (!clientFile.equals("")) client.clientFile=clientFile;
                                    world.logger.debug("Logging into: " + world.getName() + " as " + username + " with client "+clientFile+" ...\n");
                                    String remoteAddr = request.getRemoteAddr();
                                    thisPlayer = world.addPlayer(username,myProfile,client,remoteAddr);
                                    if (thisPlayer != null) {
                                        userid = thisPlayer.id;
                                        status = "CONNECTED";
                                        utils.setSession("userid",userid);
                                        utils.setSession("usernick", username);
                                        utils.setSession("worldinstanceid",world.instanceid);
                                        //thisPlayer.look(thisPlayer.container);
                                        thisPlayer.getClient().cmdRefrScene();
                                        thisPlayer.updMapPos();
                                    } else {
                                        msgError.append("Login failed\n");
                                    }
                                }
                            } else if (cmd.equals("popmsg")) {
                                //
                                // POPMSG
                                //
                                if (status.equals("DISCONNECTED") || thisPlayer == null) {
                                    msgError.append(world.msgs.msg[5]);
                                } else {
                                    // Connected or the like
                                    msgb = thisPlayer.getClient();
                                    if (msgb != null) {
                                        String msgid = utils.getForm("msgid",charset);
                                        thisMsg = msgb.popMessage(msgid);
                                    }
                                }
                            } else if (!cmd.equals("")) {
                                // Admin commands
                                if (!world.adminPasswd.equals(utils.getForm("password",charset)) && world.hooks.get(cmd) == null) {
                                    msgError.append(world.msgs.msg[8]);
                                    world.logger.debug("WARNING: Trial to perform admin operation with password=" + utils.getForm("pass",charset));
                                } else {
                                    // Password OK or HOOK command
                                    if (cmd.equals("snapshot")) {
                                        view = "snapshot"; // Forces snapshot view
                                    } else if (cmd.equals("dump")) {
                                        view = "dump"; // Forces dump view
                                    } else if (cmd.equals("clearlog")) {
                                        world.logger.clear();
                                        view = "log"; // Forces log view
                                    } else if (cmd.equals("showlog")) {
                                        view = "log"; // Forces log view
                                    } else if (cmd.equals("restart")) {
                                        status = "DISCONNECTED";
                                        thisPlayer = null;
                                        userid = "";
                                        world.logger.debug("performing RESTART upon admin command");
                                        world = initSlot(slot);
                                    } else if (cmd.equals("opti")) {
                                        view = "opti"; // Forces view
                                    } else if (cmd.equals("_addcode")) {
                                        view = "admin"; // Forces view
                                        ViewAdmin.addCode(world,utils.getForm("textarea",charset));
                                    } else if (cmd.equals("_execute")) {
                                        view = "admin"; // Forces view
                                        ViewAdmin.addCode(world,"Sub __ConsoleCommand\n" + utils.getForm("textarea",charset) + "\nEnd_Sub");
                                        world.execute("__ConsoleCommand", world, null, null, new Token(true), true);
                                        world.varGet("__ConsoleCommand",true).assign(new Token(), world); // Erase
                                    } else { // Must be a HOOK command
                                        if (Utils.isIn(cmd,world.hooks)) {
                                            if (thisPlayer == null) {
                                                thisPlayer = new Player(null, "dummy", null, null, null, 0, null, null, new Client(null, 1, false, false, 800, 600, null));
                                                thisPlayer.world = world; // patches world property
                                            }
                                            world.execute((String) world.hooks.get(cmd), world, thisPlayer, (DictSorted) kv.get("input"), new Token(true), true);
                                            view="direct";
                                        } else {
                                            msgError.append("Unknown hook/admin command: " + cmd);
                                        }
                                    }
                                }
                            }
                        } else {    // Command is NOT an offline command
                            String arg1 = cutprefix(Utils.cStr(kv.get("arg1")));
                            String arg2 = cutprefix(Utils.cStr(kv.get("arg2")));
                            if (thisPlayer != null)	{
                                msgb = thisPlayer.getClient();
                                msgb.session = utils; // Save session info for area changing
                                thisPlayer.focus = null; // Reset focus info
                            } else { // Create default client to avoid nullpointer
                                msgb = new Client(world,world.msgListSize,false,false,800,600,request.getHeader("User-Agent") );
                            }
                            if (thisPlayer != null && thisPlayer.container != null && status.equals("CONNECTED")) {
                                // CONNECTED commands
                                
                                if (cmd.equals("look")||cmd.equals("click")) {
                                    AdvObject cont = thisPlayer.container;
                                    if (arg1.equals("")) { // default - look room
                                        if (cont != null) {
                                            arg1 = cont.id;
                                        }
                                    }
                                    thisPlayer.focus = world.getObject(arg1);
                                    if (!thisPlayer.look(thisPlayer.focus,(DictSorted) kv.get("input"),(cmd.equals("look")))) {
                                        // Object was moved
                                        // if not in null then focus = room
                                        if (cont != null)
                                            thisPlayer.focus = world.getObject(cont.id);
                                    }
                                } else if (cmd.equals("go")) {
                                    //
                                    // GO
                                    //
                                    String wayId = arg1;
                                    //logger.debug("Prima sono in: " + thisPlayer.getRoom());
                                    if (!wayId.equals("")) {
                                        AdvObject cont = thisPlayer.container;
                                        if (cont != null && cont.isVehicle()) {
                                            // drive vehicle
                                            cont.go(wayId);
                                        } else {
                                            // follow link
                                            if (thisPlayer.go(wayId)) {
                                                // Looking the new room
                                                //thisPlayer.look(thisPlayer.container);
                                            }
                                        }
                                    }
                                    //logger.debug("Poi sono in: " + thisPlayer.getRoom());
                                } else if (cmd.equals("rotate")) {
                                    thisPlayer.rotate(arg1);
                                } else if (cmd.equals("open")) {
                                    AdvObject o = world.getObject(arg1);
                                    thisPlayer.objectOpen(o);
                                } else if (cmd.equals("close")) {
                                    AdvObject o = world.getObject(arg1);
                                    thisPlayer.objectClose(o);
                                } else if (cmd.equals("pick")) {
                                    // PICK
                                    if (arg1.equals("")) {
                                        Dict conts = thisPlayer.container.getContents(false);
                                        DictEnumerator e = new DictEnumerator(conts);
                                        while (e.hasMoreElements ()) {
                                            AdvObject it = (AdvObject) e.nextElement();
                                            if (it.isanItem() && !it.isHidden() && it.isPickable() && (thisPlayer.getFreeSpace() >= it.volume.intVal())) {
                                                thisPlayer.itemPick(it);
                                            }
                                        } 
                                    } else {
                                        thisPlayer.itemPick(world.getObject(arg1));
                                    }
                                } else if (cmd.equals("drop")) {
                                    // DROP
                                    thisPlayer.itemDrop(world.getObject(arg1));
                                } else if (cmd.equals("give")) {
                                    // GIVE
                                    thisPlayer.itemGive(arg1,arg2);
                                } else if (cmd.equals("put")) {
                                    // PUT
                                    thisPlayer.itemPut(arg1,arg2);
                                } else if (cmd.equals("use") || cmd.equals("use2")) {
                                    //
                                    // USE / USE WITH
                                    //
                                    if (kv.getS("arg2").equals("")) { // use
                                        thisPlayer.itemUse(arg1);
                                        thisPlayer.focus = world.getObject(arg1);
                                    } else { // use with
                                        thisPlayer.itemUse(arg1,arg2);
                                        thisPlayer.focus = world.getObject(arg2);
                                    }
                                } else if (cmd.equals("say")) {
                                    //
                                    // SAY
                                    //

                                    thisPlayer.focus = world.getObject(arg2);
                                    if (thisPlayer.focus != null) {
                                        thisPlayer.say(arg1,thisPlayer.focus);
                                    } else { // speak to current room / whole world
                                        thisPlayer.say(arg1,thisPlayer.container);
                                    }
                                    msgb.cmdNewmsg();
                                } else if (cmd.equals("search")) {
                                    //
                                    // SEARCH
                                    //
                                    if (arg1.equals("")) { // Search current place by default
                                        arg1 = thisPlayer.container.id;
                                    }
                                    thisPlayer.objectSearch(arg1);
                                } else if (cmd.equals("hide")) {
                                    thisPlayer.objectHide(arg1);
                                } else if (cmd.equals("enter")) {
                                    //
                                    // ENTER
                                    //
                                    boolean res = thisPlayer.moveTo(world.getObject(arg1),thisPlayer,Const.CHECK_OPEN,Const.CHECK_EVENTS);
                                    if (res == false) {
                                        msgb.display(world.msgs.msg[141]);
                                    }
                                } else if (cmd.equals("exit")) {
                                    //
                                    // EXIT
                                    //
                                    PeopleContainer o = thisPlayer.getPeopleContainer();
                                    if (o != null && o.isVehicle() || o.isanItem()) {
                                        thisPlayer.moveTo(o.getPeopleContainer(),thisPlayer,Const.CHECK_OPEN,Const.CHECK_EVENTS);
                                    } else {
                                        msgb.display(world.msgs.msg[178]);
                                    }
                                } else if (cmd.equals("save")) {
                                    //
                                    // SAVE
                                    //
                                    thisPlayer.saveGame(false);
                                } else if (cmd.equals("savexit")) {
                                    //
                                    // SAVE AND EXIT
                                    //
                                    status = "EXITING";  // Just switch to EXITING status
                                    msgb.cmdRefrCtrls();
                                } else if (cmd.equals("coords")) {
                                    // (SHOW) COORDS
                                    thisPlayer.say("mapx="+utils.getForm("map.x")+",mapy="+utils.getForm("map.y"),thisPlayer.container);
                                } else {
                                    //
                                    // Generic (user-defined) command
                                    //
                                    if (!cmd.equals("")) {
                                        Ctrl c = (Ctrl) world.getAllPanelButtons().getIC(cmd);
                                        if (c != null) {
                                            if (c.event == null || c.event.equals("")) {
                                                world.logger.debug("Warning: No event foreseen for command" + cmd);
                                            } else {
                                                String targid = arg2;
                                                AdvObject o;
                                                if (c.eventModel.length() > 0 && c.eventModel.charAt(0) == 'T') {
                                                    targid = arg1;
                                                    o = world.getObject(arg2);
                                                } else {
                                                    o = world.getObject(arg1);
                                                }
                                                
                                                if (false) {
                                                    /// QUI!
                                                    world.fireEvent(c.event,o,thisPlayer.id,targid,(DictSorted) kv.get("input"),false,Const.MUST_BE_DEFINED);
                                                } else {
                                                    /*
                                                    Varspace newvarsp = new Varspace();
                                                    newvarsp.varSet("input",new Token((DictSorted) kv.get("input")));
                                                    String ownerId = null;
                                                    if (o != null) ownerId = o.id;
                                                    DimxParser p = new DimxParser(world,newvarsp,0,ownerId);
                                                    p.agent = thisPlayer;
                                                    p.target = targid;
                                                    p.feed(c.event);
                                                    Token t = p.evalExpression(p.lookupToken(),0);
                                                     */

                                                    DimxObject owner = thisPlayer;
                                                    if (o != null) owner = o;
                                                    
                                                    Token t = world.evaluateExpression(c.event, owner, thisPlayer, targid, (DictSorted) kv.get("input"));
                                                }
                                            }
                                        } else
                                            msgError.append("Invalid command: >" + cmd + "<");
                                    }
                                }
                                
                                if (thisPlayer.varGet("__clearfocus").boolVal()) {
                                    thisPlayer.focus = null;
                                    thisPlayer.varGet("__clearfocus", Const.GETREF).assign(new Token(),world); // Clear flag
                                }
                            } else if (status.equals("EXITING")) {
                                if (cmd.equals("return")) {
                                    //
                                    // RETURN TO GAME
                                    //
                                    status = "CONNECTED";
                                    msgb.cmdRefrCtrls();
                                } else if (cmd.equals("savexit")) {
                                    // As for LOGOUT - perhaps could make a common subroutine
                                    thisPlayer.password = utils.cStr(arg1);
                                    if (thisPlayer.saveGame(true) && world.removePeople(thisPlayer,Const.KICKOUT,Const.DROP_ITEMS,null)) {
                                        status = "DISCONNECTED";
                                        if (msgb.musicSupport)
                                            commands = "silence;";
                                        else
                                            commands = "";
                                        commands = commands + "refresh!ctrls";
                                        thisPlayer = null;
                                        userid = "";
                                        utils.setSession("userid",userid);
                                    } else {
                                        status = "CONNECTED";
                                        msgb.cmdRefrCtrls();
                                    }
                                }
                            } else if (status.equals("CONNECTING")) {
                                if (cmd.equals("return")) {
                                    //
                                    // RETURN TO LOGIN
                                    //
                                    status = "DISCONNECTED";
                                    utils.setSession("profile",null);
                                }
                            } // closes status=CONNECTING
                            
                            // The following commands
                            // are NOT related to a specific STATUS
                            
                            if (cmd.equals("logout")) {
                                //
                                // LOGOUT
                                //
                                status = "DISCONNECTED";
                                world.removePeople(thisPlayer,Const.KICKOUT,Const.DROP_ITEMS,null);
                                if (msgb != null && msgb.musicSupport)
                                    commands = "silence;";
                                else
                                    commands = "";
                                commands = commands + "refresh!ctrls";
                                thisPlayer = null;
                                userid = "";
                                utils.setSession("userid",userid);
                            } // close logout command management
                            
                        } // close Else there is a command
                        setStatus(world, utils,status);

                        if (thisPlayer != null && view.equals("scene")) {
                            thisRoom = thisPlayer.container;
                            if (thisRoom != null) {
                                thisRoomDescription = thisRoom.getDescription(thisPlayer, (DictSorted) kv.get("input"));
                            }
                        }
                        
                    } // End if view != players
                    
                }	// END OF LOCKING - CRITICAL SECTION
                
                // Manage moveOutside queue
                if (world.moveQueue.size() > 0) {
                    world.moveOutsideNow();
                    if (thisPlayer != null) {
                        thisPlayer.focus = null;
                        /*
                        if (thisPlayer.world != world) {
                            thisPlayer.getClient().cmdRefrScene();
                            return; // quick exit
                        }
                         */
                        thisRoom = thisPlayer.container;
                        if (thisRoom != null) {
                            thisRoomDescription = thisRoom.getDescription(thisPlayer, (DictSorted) kv.get("input"));
                        }
                    }
                }
                
                if (!view.equals("players")) {
                    //
                    // Get view situation
                    //
                    
                    if (thisPlayer != null) {
                        thisPlayer.getClient().setContact();
                        thisRoom = thisPlayer.container;
                        
                        if (thisRoom == null && !status.equals("DISCONNECTED")) {
                            status = "DEAD";
                            setStatus(world, utils, status);
                        }
                        
                        //if (focus == null) focus = thisPlayer.getRoom();
                        
                        msgb = thisPlayer.getClient();
                        if (!view.equals("msgs") && msgb.clientFile==null) {
                            msgb.cmdFocusmsg();
                        }
                        
                        if (view.equals("msgs") || view.equals("scene") || view.equals("ctrls")) {
                            commands = msgb.getCommands(view);
                        }
                        
                        if (view.equals("scene")) {
                            console = msgb.getConsole();
                            if (console.equals("\n\n")) console = "";
                            rightcolumn = msgb.getRightColumn();
                            sound = utils.absolutizeUrl(msgb.getSound(),world.imagesFolder);
                        }
                        
                        if (view.equals("msgs")) { // Vista messaggi
                            // Build messages list
                            msgV = new Dict(); // current Message list
                            for (int i=0; i < msgb.getMessageCount() ; i++) {
                                Message myMsg = msgb.getMessage(i);
                                msgV.put(myMsg.getId(),myMsg);
                            }
                            sound = utils.absolutizeUrl(msgb.getSound(),world.imagesFolder);
                        } else { // Normal view
                            
                            peopleV = new Dict(); // current People list
                            itemsV = new Dict(); // current Items list
                            if (thisRoom != null) {
                                AdvObject k = null;
                                // Build people + items list
                                synchronized (world.lock) {
                                    for (int i=0; i < thisRoom.getContents().size(); i++) {
                                        k = (AdvObject) thisRoom.getContents().elementAt(i);
                                        if (!k.isHidden()) {
                                            if (k.isaCharacter()) {
                                                if (!k.id.equals(userid)) {
                                                    peopleV.put(k.id,k);
                                                }
                                            } else if (k.isanItem()) {
                                                itemsV.put(k.id,k);
                                            }
                                        }
                                    }
                                }
                            }
                            
                        }
                    }
                }
                
                //*************************************
                // Answer-to-client section
                
                Skin skin;
                if (thisPlayer == null) {
                    skin = world.getSkin("");
                } else {
                    skin = thisPlayer.skin;
                    if (skin != null) skin = world.getSkin(skin.id);
                }
                if (view.equals("msgs")) {
                    // Produce SMS message if new messages
                    if (msgb != null & sound == null && msgb.getNewMessages()) {
                        // Standard SMS sound
                        String sndnewmsg = skin.sndNewmsg;
                        if (msgb.audioSupport && !(sndnewmsg.equals(""))) {
                            sound = sndnewmsg;
                        }
                    }
                    sendMsgs(world,out, skin, msgV, commands, sound, msgError.toString());
                    
                } else if (view.equals("ctrls")) {
                    sendCtrls(world, out, thisPlayer, skin,commands,status, utils, msgError.toString());
                } else if (view.equals("scene")) {
                    sendView(world, out, thisPlayer, skin, commands, status, thisRoomDescription, itemsV, peopleV, sound, console, rightcolumn, msgError.toString());
                } else if (view.equals("snapshot")) {
                    ViewAdmin.outputHtml(world, out,skin,view,null);
                } else if (view.equals("opti")) {
                    ViewAdmin.outputOptimizer(world, out,skin);
                } else if (view.equals("log")) {
                    ViewAdmin.outputLog(world,out,skin);
                } else if (view.equals("players")) {
                    sendPlayers(world,out,utils.getForm("format",charset),skin);
                } else if (view.equals("admin")) {
                    ViewAdmin.outputHtml(world, out,skin,null,msgError.toString());
                } else if (view.equals("hof")) {
                    ViewHOF.outputHtml(world,out,skin);
                } else if (view.equals("map")) {
                    ViewMap.outputHtml(world,out,thisPlayer,skin, null);
                } else if (view.equals("direct")) {
                    ViewDirect.outputHtml(world, out,thisPlayer,null,null);
                } else if (view.equals("")) {
                    String clientFile=null;
                    if (thisPlayer!=null) clientFile = thisPlayer.getClient().clientFile;
                    sendClient(world, out, clientFile);
                } else {
                    out.println("<HTML><BODY>This view is not supported: (" + view + ")</BODY></HTML>");
                }
                
            } else {
                //
                // SERVER OFF-LINE
                //
                out.println(
                        "<HTML><BODY>Game is temporarily OFFLINE. Please retry by clicking REFRESH on your browser. (rel. " + myVersion + ")</BODY></HTML>");
                
                out.println("<!--");
                out.println("system folder:" + systemDir);
                out.println("-->");
            }
            if (world!=null) world.logger.debug("EXIT SERVICE - VIEW " + view);
            
        } catch (DimxException e) {
            //
            // DimX RUNTIME ERROR
            //
            if (world != null && world.logger != null) {
                world.logger.debug(e.getMessage());
                if (world.logger.debugMode != 0) { // debugMode = 0 means already on file
                    // Log error stack trace anyways
                    world.logger.log("\n\n--------------------------------");
                    world.logger.log(" at " + Utils.now());
                    world.logger.log(e);
                }
                try {
                    world.varGet("__optiRuntimeErrors",Const.GETREF).assign(new Token(world.varGet("__optiRuntimeErrors").numVal()+1),world);
                } catch (DimxException e2) { // we hope this won't fail
                }
            } else {
                System.err.println(e.getMessage());
                out = response.getWriter();
            }
            if (out != null) {
                utils.pageExpires();
                out.println(
                        "<HTML><BODY>The game engine has encountered the following:<P>");
                out.println(Utils.stringReplace(e.getMessage(),"\n","<BR>",false));
                out.println("<BR>Engine version: " + this.myVersion);
                out.println("<BR>Game slot: " + slot);
                if (world != null) {
                    if (!world.hideSourcePath)
                        out.print("<!-- World file: " + world.worldFile + "-->");
                    out.print("<BR>Game version: " + world.version);
                }
                out.println();
                if (world != null && world.author != null && !world.author.equals("")) {
                    out.println("<P>Please report this error to the author of this game (" + world.authoremail + ") so that it can get fixed.");
                }
                out.println("</BODY></HTML>");
            }
        } catch (Exception e) {
            //
            // ERRORE INTERNO
            //
            if (world != null && world.logger != null) {
                world.logger.debug("ERROR: Internal error performing servlet service: " + e + "\n");
                world.logger.debug(e);
                if (world.logger.debugMode != 0) { // debugMode = 0 means already on file
                    // Log error stack trace anyways
                    world.logger.log("\n\n--------------------------------");
                    world.logger.log(" at " + Utils.now());
                    world.logger.log(e);
                }
            } else {
                System.err.println("ERROR: Internal error performing servlet service: " + e + "\n");
                e.printStackTrace(System.err);
            }
            if (out != null) {
                out.println(
                        "<HTML><BODY>The server encountered an internal error. Please pass on the message below to the game administrator:<P>");
                e.printStackTrace(out);
                out.println("<BR>Engine version: " + this.myVersion);
                out.println("<BR>Game slot: " + slot);
                out.println("</BODY></HTML>");
            }
        }
        
    }
    
    
    
    
    
    
    private String cutprefix(String x) {
        //private static String[] prefixes = {"c","r","i","w"};
        
        if (x.length() > 2)
            if (Utils.isIn(x.substring(0,1),"criw") && x.charAt(1) == '!')
                // cutprefix
                return x.substring(2);
        return x;
    }
    
    /** code to be executed when the servlet shuts down */
    public void destroy() {
        Logger.echo("==================== DESTROY - " + Utils.now());
    }
    
    private String htmlCharset(World world) {
        if (world == null)
            return "";
        else
            return world.htmlCharset();
    }
    
    private void htmlCtrl(Ctrl b, StringBuffer sb, Skin skin, Player aPlayer, Utils utils) throws DimxException {
        sb.append(b.toHtml(skin,utils,aPlayer, aPlayer, null));
    }
    
    private String htmlCtrls(World world, String status, Player thisPlayer, Skin skin, Utils utils, String msg, String errs) throws DimxException {
        
        StringBuffer sb = new StringBuffer("");
        
        // Commands zone
        String targ = "scene";
        if (status.equals("DISCONNECTED") || status.equals("CONNECTING")) targ = "ctrls";
        
        sb.append("<FORM METHOD=POST ACTION=\"" + navigatorUrl + "\" TARGET=\"" + targ + "\">");
        sb.append("<INPUT TYPE=HIDDEN NAME=view VALUE=\"" + targ + "\">\n");
        if (!status.equals("CONNECTING")) {
            sb.append("<INPUT TYPE=HIDDEN NAME=arg0 VALUE=\"\">\n");
            sb.append("<INPUT TYPE=HIDDEN NAME=arg1 VALUE=\"\">\n");
            sb.append("<INPUT TYPE=HIDDEN NAME=cmd VALUE=\"\">\n");
        }
        sb.append("<TABLE CELLPADDING=10 CLASS=cmd><TR><TD ");
        sb.append("BGCOLOR=\"" + skin.panelBgColor + "\" ");
        if (!skin.panelBackground.equals("")) {
            sb.append("STYLE=\"background-image:url(" + skin.panelBackground + ")\"");
        }
        sb.append("CLASS=cmd>");
        sb.append("<IMG SRC=\""+ skin.picSpacer + "\" HEIGHT=1 WIDTH=230 VSPACE=0 HSPACE=0 ALT=\"\"><BR>");
        if (skin.ctrlbanner != null) {
            sb.append("<IMG SRC=\""+ skin.ctrlbanner.src + "\" HEIGHT=" + skin.ctrlbanner.height + " WIDTH=" + skin.ctrlbanner.width + "><BR>");
        }
        if (status.equals("CONNECTED") && thisPlayer != null) {
            //
            // status: CONNECTED
            //
            
            Vector pair = Utils.stringSplit(thisPlayer.prevPanelIds,"+");
            
            if (pair.size() == 2) {
                String pan0 = ((String) pair.elementAt(0)).substring(1);
                String pan1 = ((String) pair.elementAt(1)).substring(1);
                Panel panel = null;
                
                // Player - specific
                //sb.append("-" + pan0 + "-<BR>");
                if (!pan0.equals("")) {
                    panel = world.getPanel(pan0);
                    if (panel == null)  throw new DimxException("Unexistent panel: " + pan0);
                    for (int i=0; i < panel.buttons.size(); i++) {
                        htmlCtrl((Ctrl) panel.buttons.elementAt(i), sb, skin, thisPlayer, utils);
                    }
                    sb.append("<BR>");
                }
                
                // Room - specific
                //sb.append("-" + pan1 + "-<BR>");
                if (!pan1.equals(""))	{
                    panel = world.getPanel(pan1);
                    if (panel == null)  throw new DimxException("Unexistent panel: " + pan1);
                } else {
                    panel = world.getPanel(); // Get default
                }
                
                sb.append(panel.htmlControls(skin,utils, thisPlayer, thisPlayer, null));
                
            } else {
                sb.append(" - ERROR panel=\"" + thisPlayer.prevPanelIds + "\" pair size=" + pair.size())	;
            }
        } else if (status.equals("DEAD")) {
            //
            // status: DEAD
            //
            htmlCtrl(new Ctrl(null,Const.CTRL_BUTTON,"quicklogout",world.msgs.cmd[2],world.msgs.cmd[2],null,null,null),sb, skin, null, utils);
        } else if (status.equals("EXITING")) {
            //
            // status: EXITING
            //
            Panel panel = (Panel) world.getPanel("exiting");
            sb.append(panel.htmlControls(skin,utils,null,null,null));
        } else if (status.equals("CONNECTING")) {
            //
            // status: CONNECTING
            //
            Panel panel = (Panel) world.getPanel("connecting");
            sb.append(panel.htmlControls(skin,utils,null,null,null));
        } else {
            //
            // status: DISCONNECTED
            //
            Panel panel = (Panel) world.getPanel("connect");
            sb.append(panel.htmlControls(skin,utils,world.defaultCharacter,world.defaultCharacter,null));
        }
        sb.append("</TD></TR></TABLE></FORM>");
        if (!msg.equals("") || !errs.equals(""))  {
            sb.append("<BR><HR><BR>");
            sb.append(htmlSystemMessages(msg,null,errs));
        }
        return sb.toString();
    }
    
/*
 *
 *	ritorna InBox
 *
 */
    private String htmlInbox(World world,String currView, Dict msgV, Skin skin) throws DimxException {
        StringBuffer sb = new StringBuffer("");
        try {
            if (msgV!=null) {
                sb.append("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0 WIDTH=\"100%\">");
                // List msgs
                if (!msgV.isEmpty()) {
                    //			for (int i=msgV.size()-1; i >= 0; i--) {
                    for (int i=0; i < msgV.size(); i++) {
                        Message myMsg = (Message) msgV.elementAt(i);
                        String id = java.net.URLEncoder.encode(msgV.keyAt(i),"UTF-8");
                        //String id = msgV.keyAt(i);
                        String col = skin.list1BgColor;
                        if ((Utils.cInt(id) % 2) == 0) col = skin.list2BgColor;
                        if (!col.equals("")) col = "BGCOLOR=\"" + col + "\"";
                        String txt = myMsg.getText();
                        sb.append("<TR VALIGN=TOP " + col + "><TD WIDTH=15 ALIGN=CENTER ");
                        sb.append("BGCOLOR=\"" + skin.panelBgColor + "\" ");
                        sb.append("CLASS=listing>");
                        //sb.append("<FORM METHOD=GET><INPUT TYPE=HIDDEN NAME=view VALUE=" + currView + "><INPUT TYPE=HIDDEN NAME=cmd VALUE=popmsg><INPUT TYPE=HIDDEN NAME=msgid VALUE=\"" + id + "\"><INPUT TYPE=SUBMIT VALUE=x></FORM>");
                        sb.append("<A STYLE=\"text-decoration: none\" HREF=\"" + navigatorUrl + "?cmd=popmsg&amp;view="+currView+"&amp;msgid="+ id + "\">X</A></TD><TD WIDTH=100 CLASS=listing>"
                                + myMsg.getSenderName() + "</TD><TD CLASS=listing>");
                        sb.append(txt + "</TD></TR>\n");
                    }
                }
                sb.append("</TABLE>");
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new DimxException("Problems in encoding the message Key - htmlInbox");
        }
    }
    
    /** Outputs the HTML for the navigation pad
     * for multiface rooms
     * @param waysV Vector of available links
     * @param sb stringbuffer for output
     * @param skin current skin
     * @param thisRoom current room
     * @param facing current facing
     *
     */
    public String htmlNavpadMultiface(World world,Dict waysVorig, Skin skin, AdvObject thisRoom, String facing) {
        Dict waysV = (Dict) waysVorig.clone();
        StringBuffer sb = new StringBuffer();
        
        sb.append("<TABLE ID=\"navpad\" BORDER=0 WIDTH=" + (world.iconSize*2) + " CELLPADDING=2 CELLSPACING=0 BGCOLOR=\"" + skin.panelBgColor + "\">\n");
        
        // First row
        sb.append("<TR><TD COLSPAN=3 ALIGN=CENTER>");
        Link w = null;
        if (waysV != null) { // Find a way going forward
            for (int i = 0; i < waysV.size(); ) {
                Link wx = (Link) waysV.elementAt(i);
                if (thisRoom != null && wx.getDirection(thisRoom.id).equals(facing)) {
                    w = wx;
                    waysV.removeAt(i);
                } else {
                    i++;
                }
            }
        }
        if (w != null) { // Way in current facing - place go-forward icon
            sb.append(world.htmlIcon(w,skin,thisRoom.id,facing));
        } else { // no way - blank space
            sb.append(world.htmlIcon(null,skin,null,null));
        }
        sb.append("</TD></TR>\n");
        
        // Second row
        sb.append("<TR><TD>\n");
        String direction = Utils.nextFace(facing,"l");
        Dict myimages = thisRoom.images.dictVal();
        if (myimages.get(direction) != null) { // Image at left - place rotate left icon
            sb.append("<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('rotl');return false;\"><IMG SRC=\"" + skin.icoRotL + "\" WIDTH=16 HEIGHT=16 ALT=\"" + world.msgs.msg[173] + "\" TITLE=\"" + world.msgs.msg[173] + "\" BORDER=0></A>");
            Utils.removeWayFacing(waysV, thisRoom.id, direction);
        } else { // no way - blank space
            sb.append(world.htmlIcon(null,skin,null,null));
        }
        sb.append("</TD><TD>");
        sb.append(world.htmlIcon(null,skin,null,null));
        sb.append("</TD><TD>");
        direction = Utils.nextFace(facing,"r");
        if (myimages.get(direction) != null) { // Image at right - place rotate right icon
            sb.append("<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('rotr');return false;\"><IMG SRC=\"" + skin.icoRotR + "\" WIDTH=16 HEIGHT=16 ALT=\"" + world.msgs.msg[174] + "\" TITLE=\"" + world.msgs.msg[174] + "\" BORDER=0></A>");
            Utils.removeWayFacing(waysV, thisRoom.id, direction);
        } else { // no way - blank space
            sb.append(world.htmlIcon(null,skin,null,null));
        }
        sb.append("</TD></TR>\n");
        
        // Third row
        sb.append("<TR><TD COLSPAN=3 ALIGN=CENTER>");
        direction = Utils.getOppositeDirection(facing);
        if (myimages.get(direction) != null) { // Image opposite to current facing - place reverse icon
            sb.append("<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('rev');return false;\"><IMG SRC=\"" + skin.icoRev + "\" WIDTH=16 HEIGHT=16 ALT=\"" + world.msgs.cmd[25] + "\" TITLE=\"" + world.msgs.cmd[25] + "\" BORDER=0></A>");
            Utils.removeWayFacing(waysV, thisRoom.id, direction);
        } else { // no way - blank space
            sb.append(world.htmlIcon(null,skin,null,null));
        }
        sb.append("</TD></TR></TABLE>\n");
        
        // Other controls
        Dict	otherctrls = new Dict();
        if (waysV != null) {
            for (int i=0; i < waysV.size(); i++) {
                w = (Link) waysV.elementAt(i);
                otherctrls.put(w.id,w);
            }
        }
        sb.append(world.htmlTable(otherctrls,skin,world.msgs.msg[20],thisRoom.id,null,facing,true,true));
        
        return sb.toString();
    }
    
    /** Outputs the HTML for the navigation pad
     * @param waysV Vector of available links
     * @param sb stringbuffer for output
     * @param skin current skin
     * @param thisRoom current room
     * @param facing current facing
     */
    public String htmlNavpad(World world, Dict waysV, Skin skin, AdvObject thisRoom, String facing) {
        
        StringBuffer sb = new StringBuffer();
        
        Dict	mainctrls = new Dict(6);
        Dict	otherctrls = new Dict();
        Link w = null;
        String thisRoomId = thisRoom.id;
        String direction;
        
        if (waysV != null) {
            // Building arrays...
            for (int i=0; i < waysV.size(); i++) {
                w = (Link) waysV.elementAt(i);
                direction = Utils.getRelativeDirection(w.getDirection(thisRoomId),facing);
                if (Utils.isIn(direction,Const.compassDirections) && mainctrls.get(direction) == null) {
                    mainctrls.put(direction,w);
                } else {
                    otherctrls.put(w.id,w);
                }
            }
        }
        
        // Remove useless arrows
        // if simplified...
        if (world.simplifyNavigation) {
            String newface = Utils.rotate(thisRoom,facing,"r");
            if (!newface.equals(facing)) {
                mainctrls.remove(Utils.getRelativeDirection(newface,facing));
            }
            newface = Utils.rotate(thisRoom,facing,"l");
            if (!newface.equals(facing)) {
                mainctrls.remove(Utils.getRelativeDirection(newface,facing));
            }
        }
        
        // Main pad
        sb.append("<TABLE ID=\"navpad\" BORDER=0 WIDTH=" + (world.iconSize*2) + " CELLPADDING=2 CELLSPACING=0 BGCOLOR=\"" + skin.panelBgColor + "\">\n");
        sb.append("<TR><TD COLSPAN=3 ALIGN=CENTER>");
        sb.append(world.htmlIcon((AdvObject) mainctrls.get("N"),skin,thisRoomId,facing));
        sb.append("</TD></TR>\n");
        if (thisRoom.hasSeveralFaces()) {
            sb.append("<TR><TD>\n");
            sb.append("<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('rotl');return false;\"><IMG SRC=\"" + skin.icoRotL + "\" WIDTH=16 HEIGHT=16 ALT=\"" + world.msgs.msg[173] + "\" TITLE=\"" + world.msgs.msg[173] + "\" BORDER=0></A>");
            sb.append("</TD><TD>");
            sb.append(world.htmlIcon(null,skin,null,null));
            sb.append("</TD><TD>");
            sb.append("<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('rotr');return false;\"><IMG SRC=\"" + skin.icoRotR + "\" WIDTH=16 HEIGHT=16 ALT=\"" + world.msgs.msg[174] + "\" TITLE=\"" + world.msgs.msg[174] + "\" BORDER=0></A>");
            sb.append("</TD></TR>\n");
        }
        if (thisRoom.hasSeveralFaces() && ( mainctrls.get("W") == null && mainctrls.get("E") == null)) {
            // Rotate arrows have been displayed - skip these arrows
        } else {
            sb.append("<TR><TD WIDTH=50%>");
            sb.append(world.htmlIcon((AdvObject) mainctrls.get("W"),skin,thisRoomId,facing));
            sb.append("</TD><TD>");
            sb.append(world.htmlIcon(null,skin,null,null)); // If true omits the blank, if false, places the blank for better style
            sb.append("</TD><TD WIDTH=50%>");
            sb.append(world.htmlIcon((AdvObject) mainctrls.get("E"),skin,thisRoomId,facing));
            sb.append("</TD></TR>\n");
        }
        sb.append("<TR><TD>");
        sb.append(world.htmlIcon(null,skin,null,null));
        sb.append("</TD><TD>");
        sb.append(world.htmlIcon((AdvObject) mainctrls.get("S"),skin,thisRoomId,facing));
        sb.append("</TD><TD>");
        sb.append(world.htmlIcon(null,skin,null,null));
        sb.append("</TD></TR></TABLE>\n");
        
        
        
        //world.htmlTable(waysV,sb,skin,msgs.msg020,thisRoom.id,null,null,facing,true);
        sb.append(world.htmlTable(otherctrls,skin,world.msgs.msg[20],thisRoom.id,null,facing,true,true));
        
        return sb.toString();
    }
    
    /** Generates the HTML to display on-screen objects
     * @param things
     * @param sb
     * @param skin
     * @param facing
     * @param exclude
     * @param im
     * @param currRoom
     * @param targmin
     * @param targmax
     */
    private void htmlScreenDisplay(World world, Dict things, StringBuffer sb, Skin skin,
            Token tfacing, Vector exclusions, Image im,
            AdvObject currRoom, // Current room
            int targmin, int targmax, double factorx, boolean focused) throws DimxException {
        String facing = tfacing.strVal();
        int iconsize = world.iconSize;
        String imgsrc = null;
        // Show Area
        int showareax1 = im.showareax1;
        int showareax2 = im.showareax2;
        int baseline = im.baseline;
        int stackpos = 0; // Position on the HTML stack (z-index)
        int x=0, y=0; // Image positioning coordinates
        String xt=""; // Tag for image dimensioning (width)
        String yt=""; // Tag for image dimensioning (height)
        boolean xvalid = true; // Validity flag for the x coordinate
        boolean yvalid = true; // Validity flag for the y coordinate
        int width = 0;         // Width of the displayed image
        int height = 0;        // Height of the displayed image
        int imwidth = im.getWidth();
        int imheight = im.getHeight();
        
        //logger.debug("SHOWAREA should be " + showareax1 + " to " + showareax2 + ", baseline " + baseline);
        
        // SHOWAREA fixup
        if (showareax1 < 0) showareax1 = 0;
        if (showareax2 < 0) showareax2 = imwidth;
        if (baseline < 0) baseline = imheight * 10 / 100;
        
        //logger.debug("SHOWAREA is actually " + showareax1 + " to " + showareax2 + ", baseline " + baseline);
        
        if (things != null) {
            for (int i=0; i < things.size(); i++) {
                AdvObject o = (AdvObject) things.elementAt(i);
                
                boolean showit = focused || // Determine if should be shown - if focused YES
                        (o.showmode.intVal() != Const.OFFSCREEN && !Utils.isIn(o.id,exclusions) && !o.isHidden() );

                Image zoomImage = null; // No zoomedImage by default
                
                if (showit) {
                    boolean invisible = (o.varGet("invisible").intVal() == 1);
                    xt = "";
                    yt = "";
                    imgsrc = null;
                    width = 0;
                    height = 0;
                    if (o.showmode.intVal() == Const.ONSCREEN_IMAGE || focused) {
                        if (focused) { // Does it have a Zoom Image?
                            Token zit = o.varGet("zoomImage");
                            if (!zit.isNull()) {
                                zoomImage = zit.imageValSafe();
                            }
                        }
                        Object[] res = o.getNearestImage(facing);
                        if (zoomImage != null) {
                            res[0] = zoomImage;
                        }
                        if (res == null) {
                            world.logger.log("WARNING! This object should be ONSCREEN and has got no images: " + o.toString());
                            res = new Object[2];
                            res[0] = skin.picPlayer; // Uses default avatar
                        }
                        if (res[0] == null) {
                            world.logger.log("WARNING: No images[0] for " + o.toString() + " trying to fix it");
                            Dict ims = new Dict();
                            ims.put("N",  new Token(new Image("missing",64,100)));
                            o.images.setVal(ims);
                        }
                        Image focus_img = (Image) res[0];
                        if (invisible) { // Invisibile - use transparent GIF
                            focus_img =new Image(skin.picSpacer,focus_img.getWidth(),focus_img.getHeight());
                        }
                        if (focus_img != null) {
                            imgsrc = focus_img.getSrc();
                            width = focus_img.getWidth();
                            height = focus_img.getHeight();
                        } else {
                            showit = false;
                        }
                    } else { // ICON
                        imgsrc = o.getIcon();
                        if (imgsrc != null) {
                            width = iconsize;
                            height = iconsize;
                        }
                    }
                    
                    if (width>0) xt = " WIDTH=" + Utils.cInt(factorx*width);
                    if (height>0) yt = " HEIGHT=" + Utils.cInt(factorx*height);
                    
                    // X,Y positioning
                    xvalid = true;
                    yvalid = true;
                    
                    if (!o.showfor.isNull()) { // SHOW FOR specified
                        String sceneUrl = Utils.absolutizeUrl(o.showfor.strVal(),world.imagesFolder);
                        if (!im.getSrc().endsWith(sceneUrl)) { // Object was not designed to show up on image "im"
                            // We have 2 cases here: either it's the wrong face, or we are in another room
                            String[] faces = new String[4];
                            currRoom.copyFacesInto(faces);
                            if (Utils.isIn(sceneUrl,faces)) {
                                // Same room but wrongly faced - don't show
                                showit = false;
                            } else {
                                // current room is another / default display - except for links
                                if (o.isLink()) showit = false;
                                else {
                                    xvalid = false;
                                    yvalid = false;
                                }
                            }
                        }
                    }
                }
                if (showit) {
                    int showx = o.varGet("showx").intVal();
                    int showy = o.varGet("showy").intVal();
                    // Force auto positioning in case it is the zoomedImage
                    if (zoomImage != null) {
                        showx = Utils.cInt((imwidth-width)/2);
                        showy = Utils.cInt((imheight-height)/2);
                    }
                    // First validity check of X,Y - is it outside room boundaries?
                    if (showx < 0 || showx >= imwidth) {
                        xvalid = false;
                    }
                    if (showy < 0 || showy >= imheight) {
                        yvalid = false;
                    }
                    
                    // Patching of X coord
                    if (xvalid) {
                        // If x defined and valid then use it
                        x = showx;
                        //logger.debug(o.name + "'s x was: " + x );
                    } else {
                        // Invalid x - set rnd
                        int range = (showareax2-showareax1)-width;
                        if (range > 0)  {
                            x = showareax1 + rndGen.nextInt(range);
                        } else {
                            x = showareax1;
                        }
                    }
                    // Patching of Y coord
                    if (yvalid) {
                        // If y defined and valid then use it
                        y = im.getScreenY(showy,height);
                        stackpos = imheight-showy; // Temp
                    } else {
                        int bl=baseline; // Baseline to be used for visualisation
                        if (o.showmode.intVal() == Const.ONSCREEN_ICON)
                            bl = baseline/2;
                        
                        y = im.getScreenY(bl,height);
                        stackpos = imheight-bl; // Temp
                    }
                    
                    // Reproportion Y
                    stackpos = Utils.proportion(stackpos, 0, imheight, targmin, targmax);
                    
                    // Actual visualisation
                    sb.append("\n<DIV CLASS=\"onscreen_obj\" style=\"left:" + Utils.cInt(factorx*im.getScreenX(x)) + "; top:" + Utils.cInt(factorx*y+21) + "; position: absolute; z-index: " + stackpos + "\"><A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('");
                    sb.append(o.getTypePrefix()+o.id + "|" + Utils.stringReplace(o.getName(),"'","\\'",false));
                    sb.append("');return false;\"><IMG BORDER=0 SRC=\""+ imgsrc + "\"" + xt + yt + " ALT=\"" + o.getName() + "\" TITLE=\"" + o.getName() + "\"></A></DIV>\n");
                }
                
            }
        }
    }
    
/*
 *
 *	ritorna messaggi di sistema formattati
 *
 */
    private String htmlSystemMessages(String sysMsgs, String rightColumn, String sysErrs) {
        StringBuffer sb = new StringBuffer();
        if (rightColumn != null && !rightColumn.equals("")) {
            sb.append("<DIV CLASS=\"right_column\">");
            sb.append(rightColumn);
            sb.append("</DIV><!-- closes right_column -->\n");
        }
        if (!sysErrs.equals("")) {
            sb.append("<DIV CLASS=sysmsg>");
            sb.append(sysErrs);
            sb.append("</DIV>");
        }
        if (!sysMsgs.equals("")) {
            sb.append("<DIV CLASS=\"left_column\">");
            sb.append(Utils.stringReplace(sysMsgs,"\n","<BR>\n",false));
            sb.append("</DIV><!-- closes left_column -->\n");
        }
        return sb.toString();
    }
    
    private String htmlProperties(Player thisPlayer) {
        StringBuffer propSb = new StringBuffer();
        if (thisPlayer.world == null) { // Avoids NullPointer
            return "(DEAD)";
        }
        int attrcount = thisPlayer.varsToHtmlTable(propSb,thisPlayer.world.msgs.msg[24]);
        if (attrcount>0) {
            return propSb.toString();
        } else {
            return "";
        }
    }
    
    private String htmlSceneBanner(World world, Player thisPlayer, AdvObject focus, PeopleContainer container, String status) throws DimxException {
        StringBuffer sb = new StringBuffer();
        if (thisPlayer != null && (status.equals("CONNECTED") || status.equals("EXITING"))) {
            //
            // status: CONNECTED
            //
            // Display banner
            sb.append("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0 WIDTH=\"100%\">");
            String focusname = "";
            if (focus != null) 
                focusname = focus.getName(); 
            else if (container != null) 
                focusname = container.name.strVal();
            sb.append("<TR><TD COLSPAN=3 CLASS=title>" + focusname);
            if (world.compass || container.hasSeveralFaces()) {
                sb.append(" - " + Messages.actualize(world.msgs.msg[25],world.getDirectionStr(thisPlayer.facing.strVal())));
            }
            sb.append(" (" + thisPlayer.getName() + ")</TD></TR></TABLE>\n");
        }
        return sb.toString();
    }
    
    private String htmlScene(World world, Player thisPlayer, Skin skin, AdvObject focus, PeopleContainer currContainer, Room currRoom, Dict waysV, Dict itemsV, Dict peopleV, Image im) throws DimxException {
        StringBuffer sb = new StringBuffer();
        
        if (im!= null){
            Vector exclusions = null;
            Client client = thisPlayer.getClient();
            double factorx = client.factorx;
            int imwidth = im.getWidth();
            int imheight = im.getHeight();
            
            int scaledw = Utils.cInt(factorx*(imwidth-3));
            int scaledh = Utils.cInt(factorx*(imheight+1))-1;
            
            // Display HORIZONTAL & VERTICAL SPACERS
            sb.append("<IMG SRC=\""+ skin.picSpacer + "\" HEIGHT=1 WIDTH=" + scaledw + " ALT=\"\"><BR>\n");
            sb.append("<IMG SRC=\""+ skin.picSpacer + "\" HEIGHT=" + scaledh + " WIDTH=1 ALT=\"\"><BR>\n\n");
            
            if (!im.getSrc().equals("")) {
                
                if (im.getSrc().toLowerCase().endsWith(".gif") || im.getSrc().toLowerCase().endsWith(".png") ) {
                    // Layer 0 - Display BACKGROUND BOX
                    String bgcolor = world.varGet("bgcolor").strVal();
                    if (bgcolor.equals("")) {
                        bgcolor = "#9DB9E9";
                    }
                    sb.append("<DIV class=\"scene_background\" style=\"left:1;top:21;position:absolute;z-index:0;height:"+scaledh+";width:"+scaledw+";background-color:" + bgcolor + ";\">&nbsp;</DIV>");
                }
                // Layer 2 - Display ROOM
                
                sb.append("<DIV class=\"scene_image\" style=\"left: 1; top: 21; position: absolute; z-index: 2\">");
                View.htmlImage(im, sb, client, currContainer.getName(),null,null,null);
                sb.append("</DIV>\n");
                
                // Layer 3 - Display EFFECT
                Token t = currContainer.varGet("effect");
                if (t.isImage()) {
                    Image effect = t.imageVal();
                    
                    sb.append("<DIV class=\"scene_effect\" style=\"left: 1; top: 21; position: absolute; z-index: 3\">");
                    View.htmlImage(effect, sb, client, currContainer.getName(),null,null,null);
                    sb.append("</DIV>\n");
                }
                
                // Layers 6-14 ROOM ways
                
                htmlScreenDisplay(world, currRoom.getLinks(),sb,skin,thisPlayer.facing,null, im, currContainer,6,14, factorx, false);
                // The following must be taken from actualRoom
                
                // Layers 15-204 ROOM contents
                exclusions = new Vector();
                if (world.sceneLook == Const.LOOK_1STPERSON) exclusions.add(thisPlayer.id);
                exclusions.add(currContainer.id);
                if (focus != null) exclusions.add(focus.id);
                htmlScreenDisplay(world, currRoom.getContents(),sb,skin,thisPlayer.facing,exclusions, im, currContainer,15,204, factorx, false);
                
                if (!currContainer.isaRoom()) {
                    // Display inner view
                    
                    // Layer 205 - Display ITEM/VEHICLE INTERIORS
                    
                    im = currContainer.varGet("innerImage").imageVal();
                    if (im != null) {
                        String image = im.getSrc();
                        sb.append("\n<DIV class=\"vehicle_interiors\" style=\"left: 1; top: 21; position: absolute; z-index: 201\">");
                        sb.append("<IMG BORDER=0 WIDTH=" + Utils.cInt(factorx*imwidth) + " HEIGHT=" + Utils.cInt(factorx*imheight) + " SRC=\""+ image + "\" ALT=\"" + currContainer.getName() + "\" TITLE=\"" + currContainer.getName() + "\">");
                        sb.append("</DIV>\n\n");
                    }
                    
                    // Layers 206-254 INTERNAL OBJECTS (w/people)
                    
                    Token tspp = currContainer.varGet("showPolicyPeople");
                    htmlScreenDisplay(world, itemsV,sb,skin,thisPlayer.facing,exclusions, im, currContainer,206,254, factorx, false);
                    if (tspp.intVal() == 1) {// Force on-screen people (default would be off-screen)
                        htmlScreenDisplay(world, peopleV,sb,skin,thisPlayer.facing,exclusions, im, currContainer,206,254, factorx, false);
                        peopleV.clear(); // Prevent people from being shown as icons
                    }
                    
                }
                
                // Do we have a specific focused object?
                if (focus != null && focus != currContainer && focus.isAccessibleFrom(thisPlayer)) {
                    
                    Dict d = new Dict();
                    
                    // Layers 255 FADER
                    
                    if (focus.getNearestImage(thisPlayer.facing.strVal()) != null) {
                        String link = "<A HREF=\"#\" onMouseOver=\"window.status=window.defaultStatus;return true;\" onClick=\"parent.command('" + currContainer.getTypePrefix()+currContainer.id + "|" + Utils.stringReplace(currContainer.getName(),"'","\\'",false) + "');return false;\">";
                        sb.append("<DIV class=\"fader_box\" style=\"left: 1; top: 21; position: absolute; z-index: 255\">" + link + "<IMG BORDER=0 WIDTH=" + Utils.cInt(factorx*imwidth) + " HEIGHT=" + Utils.cInt(factorx*imheight) + " SRC=\""+ thisPlayer.skin.picFader + "\"></A></DIV>\n");
                        
                        // Layers 256-260 FOCUSIZED OBJECT
                        d.put("focus",focus);
                    }
                    
                    htmlScreenDisplay(world,d,sb,skin,thisPlayer.facing,null, im, currContainer,256,260, factorx, true);
                    
                }
                
            }
        }
        
        return sb.toString();
    }
    
    private String htmlIcons(World world, Image im,
            Skin skin,
            AdvObject focus,
            PeopleContainer container,
            Dict itemsV,
            Dict peopleV) {
        StringBuffer sb = new StringBuffer();
        boolean forceShowIcons = (im == null);
        boolean fsi = forceShowIcons;
        if (container != null && !container.isaRoom()) fsi = true; // If in vehicle || ITEM then show icons only
        sb.append(world.htmlTable(peopleV,skin,world.msgs.msg[21],null,focus, null,false,fsi));
        sb.append(world.htmlTable(itemsV,skin,world.msgs.msg[22],null,focus, null,false,forceShowIcons));
        return sb.toString();
    };
    
    private String htmlInventory(World world,Player thisPlayer,
            Skin skin,
            AdvObject focus) {
        StringBuffer sb = new StringBuffer();
        sb.append(world.htmlTable(thisPlayer.getContents(),skin,world.msgs.msg[23],null,focus,null,false,true));
        return sb.toString();
    };
    
 /*
  * ATTENTION
  *
  * Questa routine ha accesso all'oggetto Room corrente
  * ma non dovrebbe MAI leggere dai vettori people/msgs
  * ne' modificare lo stato delle cose
  * (es. prelevare messaggi)
  *
  */
    private String htmlView(World world, String status,
            Player thisPlayer,
            Skin skin,
            AdvObject focus,
            PeopleContainer container,
            Dict waysV,
            Dict itemsV,
            Dict peopleV,
            String msg,
            String rightColumn,
            String errs) throws DimxException {
        
        // Layers
        // 1 = Scene picture
        // 2-10 passages (upon y coord)
        // 11-200 Room Contents (upon y coord)
        // 201 - (Opt) Vehicle/Item interior
        // 202-250 (Opt) Vehicle/Item contents (upon y coord)
        // 251 Fade card
        // 252 Focus item
        StringBuffer sb = new StringBuffer("");
        StringBuffer inbox = new StringBuffer("");
        
        if (thisPlayer != null && (status.equals("CONNECTED") || status.equals("EXITING"))) {
            //
            // status: CONNECTED
            //
            
            // Get current rooms' image - display banner
            Image im = thisPlayer.getImageAndCorrectFacing(container);
            
            // Display banner
            sb.append("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=0 WIDTH=\"100%\">");
            String focusname = "";
            if (focus != null) focusname = focus.getName(); else if (container != null) focusname = container.name.strVal();
            sb.append("<TR><TD COLSPAN=3 CLASS=title>" + focusname);
            if (world.compass || container.hasSeveralFaces()) {
                sb.append(" - " + Messages.actualize(world.msgs.msg[25],world.getDirectionStr(thisPlayer.facing.strVal())));
            }
            sb.append(" (" + thisPlayer.getName() + ")</TD></TR>\n");
            sb.append("<TR VALIGN=TOP><TD CLASS=text>\n");
            
            // DO we have an image for current container?
            sb.append(htmlScene(world,thisPlayer,skin,focus,container,thisPlayer.getRoom(),waysV,itemsV,peopleV,im));
            
            sb.append(htmlSystemMessages(msg,rightColumn,errs));
            sb.append("</TD>\n");
            
            // Icons panel
            sb.append("<TD CLASS=text>\n");
            //sb.append("<hr style=\"width:100%;color:#fff\" />");
            sb.append("<img src=\""+ skin.picSpacer);
            // *navpad
            int cwidth=Utils.cInt(thisPlayer.getClient().factorx*160);
            sb.append("\" height=1 width="+cwidth+" ><br/>");
            
            Room r = thisPlayer.getRoom();
            if (r != null) {
                if (r.hasSeveralFaces()) {
                    sb.append(htmlNavpadMultiface(world,waysV,skin,thisPlayer.getRoom(),thisPlayer.facing.strVal()));
                } else {
                    sb.append(htmlNavpad(world,waysV,skin,thisPlayer.getRoom(),thisPlayer.facing.strVal()));
                }
            }
            sb.append(htmlIcons(world,im, skin, focus, container, itemsV, peopleV));
            sb.append(htmlInventory(world,thisPlayer,skin,focus));
            sb.append(htmlProperties(thisPlayer));
            
            sb.append("</TD></TR></TABLE>\n");
            
        } else if (status.equals("DEAD")){
            //
            // status: DEAD
            //
            if (!world.logoSrc.equals("")) {
                sb.append("<DIV style=\"position: absolute; top: 1; z-index: 1; width: 100%; text-align:center;\">\n");
                View.htmlImage(new Image(world.logoSrc,0,0), sb, world.defaultClient, "Logo","logo-splash",null,"width:100%;");
                sb.append("</DIV>\n");
            }
            sb.append("<div class=\"text gameover\" style=\"position: absolute; top: 21; z-index: 2; width: 100%; text-align:center;\">\n");
            sb.append("<H2>" + world.msgs.msg[179] + "</H2>");
            if (!msg.equals("") || !errs.equals(""))  {
                sb.append("<BR><HR><BR>");
                sb.append(htmlSystemMessages(msg,rightColumn,errs));
            }
            sb.append("</div>");
        } else {
            //
            // status: DISCONNECTED or CONNECTING
            //
            if (!world.logoSrc.equals("")) {
                sb.append("<DIV style=\"position: absolute; top: 1; z-index: 1; width: 100%; text-align:center;\">\n");
                View.htmlImage(new Image(world.logoSrc,0,0), sb, world.defaultClient, "Logo", "logo-splash",null,"width:100%;");
                sb.append("</DIV>\n");
            }
            
            sb.append("<div class=\"text splash\" style=\"position: absolute; top: 21; z-index: 2; width: 100%; text-align:center;\">\n");
            //sb.append("<span style=\"top: 21; position: absolute; z-index: 2; \">\n");
            //sb.append("<TABLE width=\"100%\"><TR><TD CLASS=text align=center>\n");
            sb.append(world.msgs.msg[1] + "<H2>" + world.getName() + "</H2>\n");
            if (!world.author.equals("")) {
                sb.append("by <H4>");
                if (!world.authoremail.equals(""))
                    sb.append("<A HREF=\"mailto:" +world.authoremail+"\">" + world.author + "</A>");
                else
                    sb.append(world.author);
                sb.append("</H4>\n");
            }
            if (!world.version.equals("")) {
                sb.append("ver. " + world.version + "<BR>");
            }
            if (!world.site.equals("")) {
                sb.append("<BR><A HREF=\"" + world.site + "\" TARGET=\"_blank\">" + world.msgs.cmd[16] + "</A><BR>\n");
            }
            sb.append("<BR><A HREF=\"" + navigatorUrl + "?view=hof\">" + world.msgs.msg[154] + "</A><BR>\n");
            sb.append("<P>\n");
            Token fptext = world.varGet("frontpagetext");
            if ( fptext != null ) {
                sb.append(fptext.strVal() + "<BR>\n");
            }
            if (world.players.size() > 0) {
                sb.append("<BR><HR><BR>\n");
                sb.append("("+world.players.size()+" " + world.msgs.msg[2]+ ")<BR>\n");
            }
            if (!msg.equals("") || !errs.equals(""))  {
                sb.append("<BR><HR><BR>");
                sb.append(htmlSystemMessages(msg,rightColumn,errs));
            }
            sb.append(world.counterHtml);
            sb.append("</div>");
            //sb.append("</TD></TR></TABLE></SPAN>");
        }
        
        return sb.toString();
    }
    
    private void initServer() throws DimxException {
        ServletContext context = this.getServletContext();
        
        synchronized (serverlock) {
            if (worlds != null) return; // To prevent double load
            Logger.echo("DimensioneX is starting up. - Rel." + myVersion );
            systemDir = getInitParameter("base");
            if (systemDir == null) {
                Logger.echo("Init parameter named \"base\" is missing. Trying to figure out system folder...");
                if (context == null) {
                    System.err.println("No context - unable to detect host system type. Giving up.");
                } else {
                    systemDir = Utils.getSystemDir(context);
                }
            } else {
                Logger.echo("Init parameter named \"base\" indicate that system folder is: " + systemDir);
            }
            
            context.setAttribute("systemDir", systemDir);
            context.setAttribute("server", this);
            
            // Initialize worlds and clusters collecions
            worlds = new DictSorted();
            clusters = new DictSorted();
            
            //Load settings
            loadSettings(systemDir + "dimensionex.properties");
        }
    }
    
    private World initSlot(String slot) throws DimxException {
        Logger.echo("Initializing game slot: " + slot);
        ServletContext context = this.getServletContext();
        // Set default values
        String propFile = systemDir + "worldnav" + slot + ".properties";
        Logger.echo("Attempt to read parameters from configuration file: " + propFile);
        
        Dict settings = loadWorldSettings(slot,propFile);
        // Now load world
        World world = loadWorld(slot,settings.getS("worldFile"),settings.getS("msgsFile"), (Logger) settings.get("logger"));
        world.adminPasswd = settings.getS("adminPasswd");
        world.analytics = settings.getS("analytics"); 
        world.clientFile = settings.getS("clientFile");
        world.clientScript = settings.getS("clientScript");
        world.debugging = (Utils.cInt(settings.get("debugMode"))>0);
        world.disableAutoRestore = (Utils.cInt(settings.get("disableAutoRestore"))>0);
        world.hideSourcePath = (Utils.cInt(settings.get("hideSourcePath"))>0);
        world.navigatorUrl = navigatorUrl;
        world.stopwords = Utils.stringSplit(settings.getS("stopwords"),",");
        world.tracing = (Utils.cInt(settings.get("tracing"))>0);
        
        int tmpTickLength=Utils.cInt(settings.get("tickLength"));
        if (tmpTickLength!=0) {
            if ((tmpTickLength>=5) && (tmpTickLength<=60)) {
                world.tickLength = tmpTickLength;
            } else {
                world.logger.log("invalid tickLength:"+tmpTickLength);
            }
        }
        
        worlds.put(slot, world);
        
        context.setAttribute("propFile" + slot, propFile);
        context.setAttribute("world" + slot, world);
        
        String clusterid = world.cluster;
        if (!clusterid.equals("")) {
            if (getCluster(clusterid) == null) {
                clusters.put(clusterid, new Cluster(clusterid,this));
            }
        }
        ((Cluster) clusters.get(world.cluster)).registerWorld(world.id);
        
        String jfile; // Set up cluster's journal
        if ((jfile = settings.getS("journal")) != null)
            world.getCluster().journal = new Journal(jfile);
        
        java.sql.Connection dbConn = null;
        if (isDBConfigured()) {
            world.getCluster().dbConnected = true;
            dbConn = world.getCluster().dbConn(null);
        }
        
        if (world.tracing) { // Verify DB is up and tracing table is there
            if (dbConn != null) {
                java.sql.Statement stmt = null;
                try {
                    stmt = dbConn.createStatement();
                    java.sql.ResultSet rs = Utils.queryDatabase("DESCRIBE `trace_" + world.id + "`", stmt, true);
                    if (rs == null)  {
                        String sql = "CREATE TABLE `trace_" + world.id + "` (`user` VARCHAR( 10 ) ,`nickname` VARCHAR( 50 ) ," +
                                "`room` VARCHAR( 100 ) ,`status` VARCHAR( 15 ) ,`command` VARCHAR( 20 ) ,`args` TEXT," +
                                "`view` VARCHAR( 20 ) ,`date_contact` DATETIME, `id` BIGINT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`), INDEX (`nickname`,`date_contact`)) CHARACTER SET utf8;" ;
                        boolean res = Utils.executeSQLCommand(sql,dbConn, true, false);
                        if (!res) {
                            world.logger.log("Could not create tracing table. Tracing disabled. sql=" + sql);
                            world.tracing = false;
                        }
                    } else rs.close();
                } catch (java.sql.SQLException ex) {
                    throw new DimxException("DB Problem: " + ex.toString());
                } finally {
                    try {
                        if (stmt != null) stmt.close();
                        dbConn.close();
                    } catch (Exception ex) {}
                }
            } else {
                world.logger.log("No connection to the database: Tracing disabled.");
                world.tracing = false;
            }
        }
        
        world.start();
        
        return world;
    }
    
    /**
     * Generates the Javascript needed for ALL buttons management
     * Creation date: (24/09/2003 17.14.29)
     * @return java.lang.String
     */
    private String jscrButtonDefs(World world) {
        StringBuffer sb = new StringBuffer();
        
        Dict allPanelButtons = world.getAllPanelButtons();
        
        sb.append("// These definitions are created by DimxServlet.jscrButtonDefs\n");
        for (int i=0; i < allPanelButtons.size(); i++) {
            Ctrl b = (Ctrl) allPanelButtons.elementAt(i);
            if (b.type == Const.CTRL_BUTTON || b.type == Const.CTRL_GHOST || b.type == Const.CTRL_IMAGEBUTTON) {
                sb.append("cmds[" + i + "] = \"" + b.id + "\"; ");
                sb.append("cmdDescs[" + i + "] = \"" + b.description + "\"; ");
                sb.append("cmdNoargs[" + i + "] = " + b.getNoArgs() + "; ");
                sb.append("cmdSrcargs[" + i + "] = \"" + b.eventModel + "\";\n");
            }
        }
        
        sb.append("var cmdsCount=" + allPanelButtons.size() + ";\n");
        sb.append("var msg026=\"" + world.msgs.msg[26] + "\";\n");
        sb.append("var msg027=\"" + world.msgs.msg[27] + "\";\n");
        sb.append("var msg028=\"" + world.msgs.msg[28] + "\";\n");
        sb.append("var msg173=\"" + world.msgs.msg[173] + "\";\n");
        sb.append("var msg174=\"" + world.msgs.msg[174] + "\";\n");
        sb.append("var msg175=\"" + world.msgs.msg[175] + "\";\n");
        sb.append("var msg176=\"" + world.msgs.msg[176] + "\";\n");
        sb.append("var cmd025=\"" + world.msgs.cmd[25] + "\";\n");
        return sb.toString();
    }
    
/*
 *
 *	Returns null if accepted
 *
 */
    
    public String requestMovement(AdvObject what,String areaid) {
        String slot = getSlot(areaid);
        
        if (slot == null) {
            World world = what.world;
            if (world != null) {
                return world.msgs.actualize(world.msgs.msg[182],areaid);
            } else { 
                return "World \"" + areaid + "\" is not loaded or does not exist";
            }
        } else {
            World w = (World) worlds.get(slot);
            if (!w.cluster.equalsIgnoreCase(what.world.cluster)) {
                return "World \"" + areaid + "\" does not belong to the current cluster";
            }
            String result = null;
            result = w.requestMovement(what);
            return result;
        }
    }
    
    /**
     * Moves an object withing a cluster, with world locking and consistency verification.
     * @param fromworld source world
     * @param o object to be moved
     * @param areaid target world's ID
     * @return null if successful, non-null String result in case of errors
     */
    public String movement(World fromworld,AdvObject o,String areaid) {
        try {
            String slot = getSlot(areaid);
            if (slot == null) {
                return "World \"" + areaid + "\" is not loaded or does not exist";
            } else {
                World toWorld = (World) worlds.get(slot);
                synchronized (toWorld.lock) { // Locks this world in order to avoid interferences
                    toWorld.verifyConsistency(); // Verify before...
                    o.worldChange(toWorld, null,null);
                    o.afterWorldChange();
                    //String res = movementNow(fromworld, o, w, slot, w.defaultRoom); // Actual movement
                    toWorld.verifyConsistency(); // Verify after!
                    return null;
                }
            }
        } catch (Exception e) {
            return "Caught exception - operation failed (" + e.getMessage() + ")";
        }
    }
    
    /*
    * if clientFile is null, world's default is used
    */
    private void sendClient(World world, PrintWriter out,String clientFile) throws DimxException {
        if (clientFile==null) {
            clientFile=world.clientFile;
        }
        String s = Utils.fetch(clientFile,world.encoding);
        
        String cs = null;
        // provide backwards compatibility when no clientScript= is present
        try{ cs = Utils.fetch(world.clientScript,world.encoding);}
        catch(DimxException x){
            cs = Utils.fetch(systemDir + world.clientScript,world.encoding);
        }
        
        String htmlCharset = "";
        if (world != null) htmlCharset = world.htmlCharset();
        
        s = Utils.stringReplace(s,"<HEAD>\n","<HEAD>\n" + htmlCharset,Const.IGNORE_CASE);
        s = Utils.stringReplace(s,"$GameName$",world.getName(),Const.IGNORE_CASE);
        s = Utils.stringReplace(s,"$ClientScript$",cs,Const.IGNORE_CASE);
        s = Utils.stringReplace(s,"$NavigatorUrl$",navigatorUrl,Const.IGNORE_CASE);
        s = Utils.stringReplace(s,"$ButtonDefs$",jscrButtonDefs(world),Const.IGNORE_CASE);
        s = Utils.stringReplace(s,"$MsgRefreshRate$",Utils.cStr(1000*msgRefreshRate),Const.IGNORE_CASE);
        
        out.println(s);
    }
    
/*
 * Output BOARD PRINCIPALE
 *
 */
    private void sendCtrls(World world,
            PrintWriter out /* */,
            Player thisPlayer,
            Skin skin,
            String commands,
            String status,
            Utils utils,
            String errs) throws java.io.IOException, DimxException {
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<HTML><head>");
        out.println(htmlCharset(world));
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.print("<title>" + world.getName() + " - Ctrls</title>\n" + skin.toHtml());
        out.print("<meta name=\"HandheldFriendly\" content=\"True\" />\n" +
        "<meta name=\"viewport\" content=\"width=device-width, user-scalable=1\" />\n");
        out.print("<script>\n"
                + "function inIframe () {\n" 
                + "    try {\n"
                + "        return window.self !== window.top;\n"
                + "    } catch (e) {\n"
                + "        return true;\n"
                + "    }\n"
                + "}"
                + "</script>\n");
        out.print("</head>\n<BODY ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        if (commands != null && !commands.equals("")) {
            out.print("onLoad=\"parent.clientExecute('" + commands + "')\" ");
        }
        if (status.equals("CONNECTED")) {
            //out.println ("onLoad=\"this.document.forms[0].txtBox.focus()\" ");
        }
        
        if (world.debugging && (status.equals("DISCONNECTED") || status.equals("CONNECTING")) && world.playersCounter==0 ) {
            out.println("onLoad=\"this.document.forms[0].submit()\" ");
        }
        out.println(">\n");
        
        String s = htmlCtrls(world, status,thisPlayer,skin,utils,"",errs);
        out.println(s);
        
        if (world.analytics!=null) out.println(world.analytics);

        out.println("</BODY>\n</HTML>\n");
        
    }
    
/*
 * Output MESSAGGI
 *
 */
    private void sendMsgs(World world,PrintWriter out,
            Skin skin,
            Dict msgV,
            String commands,
            String sound,
            String msgError)  throws java.io.IOException, DimxException {
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println(htmlCharset(world));
        out.println("<title>Messages</title>\n" + "\n");
        out.println(skin.toHtml());
        out.println("</head><body ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        if (commands != null && !commands.equals("")) {
            out.print("onLoad=\"parent.clientExecute('" + commands + "')\" ");
        }
        out.println(">\n");
        out.println(htmlInbox(world,"msgs",msgV,skin));
        out.println(htmlSystemMessages("",null,msgError));
        if (sound != null) {
            //out.print("<embed src=\"" +sound + "\" hidden=\"true\" autostart=\"true\" loop=\"false\">");
            out.print("<audio id=\"sndplayer\" autoplay ><source src=\""+sound+"\" type=\"audio/wav\"></audio>");
        }
        out.println("</body>\n</html>\n");
        
    }
    
/*
 * Output PLAYERS
 *
 */
    private void sendPlayers(World world,PrintWriter out, String format,
            Skin skin) throws java.io.IOException {
        
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.print("<HTML><HEAD><TITLE>" + world.getName() + " - Players</TITLE>\n");
        if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
        out.print(skin.toHtml() + "\n\n</HEAD>\n<BODY ");
        out.print("BGCOLOR=\"" + skin.bodyBgColor + "\" ");
        if (!skin.bodyBackground.equals("")) {
            out.print("BACKGROUND=\"" + skin.bodyBackground + "\" ");
        }
        out.println("STYLE=\"font-family: tahoma,Helvetica;font-size:10pt\" >");
        
        int s = world.players.size();
        if (s > 0) {
            out.println(s + " " + world.msgs.msg[2] + "\n");
        } else {
            out.println("Server up!\n");
        }
        
        if (format.equalsIgnoreCase("extended")) {
            out.println("<br/><br/>");
            for (int i=0; i < world.players.size(); i++) {
                Player x = (Player) world.players.elementAt(i);
                AdvObject c = x.container;
                if (c == null) {
                    out.println("<b>" + x.getName() + "</b> is in NOWHERE<br/>\n");
                } else {
                    out.println("<b>" + x.getName() + "</b> is in " + c.getName() + "<br/>\n");
                }
            }
        }
        
        out.println("</BODY>\n</HTML>\n");
        
    }




/*
 * Output BOARD PRINCIPALE
 *
 */
    private void sendView(World world,
            PrintWriter out,
            Player thisPlayer,
            Skin skin,
            String commands,
            String status,
            String thisRoomDescription,
            Dict itemsV,
            Dict peopleV,
            String sound,
            String msgNorm,
            String rightColumn,
            String msgError) throws java.io.IOException,  DimxException {
        
        // Prepare </BODY> tag
        String bodyCloseTag = "</BODY>";
        if (sound != null) {
            bodyCloseTag = "<audio id=\"sndplayer\" autoplay >\n" +
"    <source src=\"" +sound + "\" type=\"audio/wav\">\n" +
"</audio>";
        }
        
        Dict replacements = new Dict(); // Replacements for custom view
        
        Dict waysV = null;
        PeopleContainer container = null; // player's current container
        Room    currRoom = null;        // player's current room
        AdvObject focus = null;
        if (thisPlayer != null) {
            focus = thisPlayer.focus;
            container = thisPlayer.getPeopleContainer();
            currRoom = thisPlayer.getRoom();
            if (container != null) {
                waysV = container.getLinks();
                if (focus == currRoom || focus == null) {
                    // Old version
                    //if (msgNorm.equals("")) msgNorm = currRoom.getDescription();
                    // Requested by Ferion
                    if (!msgNorm.equals("")) msgNorm = msgNorm + "\n";
                    msgNorm = msgNorm + thisRoomDescription;
                }
            }
        }
        
        Page customView = null;
        String transition = "";
        if (thisPlayer != null) {
            customView = thisPlayer.view;
            // if no player-specific custom view look for world-specific custom view
            if (customView==null) {
                customView = world.sceneTemplate;
                // transitions only for persistent view, not one-time player view/special effect
                if (status.equals("CONNECTED"))
                    transition = "<META http-equiv=Page-Enter content=\"blendTrans(duration=0.5)\">\n";
            }
        }
        
        if (customView != null) {
            // USING CUSTOM VIEW
            // Prepare replacements
            replacements.put("<HEAD>","<HEAD>" + skin.toHtml() + "\n" + htmlCharset(world) + 
                             "\n<META HTTP-EQUIV=\"imagetoolbar\" CONTENT=\"no\">\n"+ 
                             transition);//may be nothing
            replacements.put("<BODY>",skin.htmlBodyTag(commands));
            replacements.put("</BODY>",bodyCloseTag);
            Image im = null;
            thisPlayer.getImageAndCorrectFacing(container);
            String banner = "";
            String navpad = "";
            String scene = "";
            if (currRoom != null) {
                banner = htmlSceneBanner(world, thisPlayer, focus, container, status);
                navpad = htmlNavpad(world,waysV,skin,currRoom,thisPlayer.facing.strVal());
                scene = htmlScene(world,thisPlayer,skin,focus,container,currRoom,waysV,itemsV,peopleV,im);
            }
            replacements.put("$banner",banner);
            replacements.put("$navpad",navpad);
            replacements.put("$scene",scene);
            
            replacements.put("$display",htmlSystemMessages(msgNorm,rightColumn,msgError));
            replacements.put("$icons",htmlIcons(world,im, skin, focus, container, itemsV, peopleV));
            replacements.put("$inventory",htmlInventory(world,thisPlayer,skin,focus));
            replacements.put("$properties",htmlProperties(thisPlayer));
            
            out.println(customView.toHtml(replacements));
            
            thisPlayer.view = null; // Restore to default
        } else {
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
            out.println("<HTML><HEAD>");
            out.println(htmlCharset(world));
            out.println("<META HTTP-EQUIV=\"imagetoolbar\" CONTENT=\"no\">");
            if (status.equals("CONNECTED")) {
                out.println("<META http-equiv=Page-Enter content=\"blendTrans(duration=0.5)\">\n");
                //<META http-equiv=page-exit content=blendtrans(duration=0.5)>
                String x = Utils.getParentFolder(Utils.getParentFolder(this.navigatorUrl));
                out.println("<script type=\"text/javascript\" src=\""+x+"ajax.js\"></script>");
            }
            out.println("<TITLE>" + world.getName() + " - Scene</TITLE>");
            if (skin.stylesheet != null) out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + skin.stylesheet + "\" />\n");
            out.println(skin.toHtml() + "\n\n</HEAD>");
            out.println(skin.htmlBodyTag(commands));
            
            out.println(htmlView(world,
                    status, thisPlayer, skin,
                    focus, container, waysV, itemsV, peopleV,
                    msgNorm,rightColumn,msgError));
            
            out.println(bodyCloseTag + "\n</HTML>\n");
        }        
    }
    
    private void setStatus(World world, Utils utils, String newStatus) {
        String prevStatus = utils.gSession("status");
        if (!prevStatus.equals(newStatus)) {
            world.logger.debug("Status change: " + prevStatus + "-->" + newStatus);
            
            if (prevStatus.equals("CONNECTED") && newStatus.equals("DISCONNECTED")) {
                world.logger.debug("Status change: " + prevStatus + "-->" + newStatus);
            }
            
            utils.setSession("status",newStatus);
        }
    }
    private DictSorted makeClustersList(String alist) {
        Dict clist = Utils.string2set(alist,";",":",true);
        DictSorted rlist = new DictSorted();
        for (int i=0; i < clist.size(); i++) {
            rlist.put(clist.keyAt(i),Utils.stringSplit((String) clist.elementAt(i),","));
        }
        return rlist;
    }
    
    public String htmlAreasPullUp(String clusterid,String excluded) {
        Vector slots = (Vector) clusterslist.get(clusterid);
        StringBuffer sb = new StringBuffer();
        if (slots != null) {
            for (int i=0; i < slots.size(); i++) {
                String slot = (String) slots.get(i);
                if (!slot.equalsIgnoreCase(excluded)) {
                    sb.append("<IFRAME HEIGHT=26 WIDTH=100 SCROLLING=\"no\" SRC=\"/dimx/servlet/multiplayer?game=" + slot + "&view=players\"></IFRAME>\n");
                }
            }
            return sb.toString();
        }
        return "";
    }
    
    /**
     * Checks for forbidden Chars (for username)
     * @param aString
     * @return true if it is valid, or false if not
     */   
    private String huntForbiddenChars(String aString) {
        String upped=aString.toUpperCase();
            int d;
        
         for (int i = 0; i < aString.length(); i++) {
            d=(int) (upped.substring(i,i+1).charAt(0));
            if (!(((d>=48)&&(d<=57)) || (d==32) || ((d>=65)&&(d<=90)))) {
                // If not number or space or letter...
                return aString.substring(i,i+1);
            }
        }        
        return null; // Tests passed - valid        
    }

    
}
