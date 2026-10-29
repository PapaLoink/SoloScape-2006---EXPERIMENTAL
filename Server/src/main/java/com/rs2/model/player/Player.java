package com.rs2.model.player;

import com.rs2.ConnectionThrottle;
import com.rs2.Server;
import com.rs2.ServerSettings;
import com.rs2.bot.BotPlayer;
import com.rs2.bot.BotRoute;
import com.rs2.bot.BotTaskDefinition;
import com.rs2.bot.BotTradeAdvertManager;
import com.rs2.bot.DropPartyBotManager;
import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.bot.combat.BotCombatLoadoutManager;
import com.rs2.bot.combat.BotCombatLoadoutTables;
import com.rs2.bot.route.BotWorldRoute;
import com.rs2.bot.route.BotWorldRouteChoice;
import com.rs2.bot.route.BotWorldRouteWalker;
import com.rs2.cache.CacheArchiveEntry;
import com.rs2.cache.CacheDefinitionIndex;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.Entity;
import com.rs2.model.EntityReference;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.bankpin.BankPinManager;
import com.rs2.model.combat.AttackValidationResult;
import com.rs2.model.combat.CombatAction;
import com.rs2.model.combat.CombatCycleEvent;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.combat.CombatType;
import com.rs2.model.combat.PvpCombatReference;
import com.rs2.model.combat.WeaponProfile;
import com.rs2.model.combat.effect.PoisonEffect;
import com.rs2.model.combat.hit.HitDefinition;
import com.rs2.model.combat.hit.HitType;
import com.rs2.model.combat.special.SpecialAttackDefinition;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.gameplay.barrows.BarrowsManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.duel.DuelArenaLocationManager;
import com.rs2.model.gameplay.duel.DuelController;
import com.rs2.model.gameplay.duel.DuelInterfaceManager;
import com.rs2.model.gameplay.duel.DuelSession;
import com.rs2.model.gameplay.fightcave.FightCaveController;
import com.rs2.model.gameplay.godwars.GodWarsDungeonManager;
import com.rs2.model.gameplay.magetrainingarena.AlchemistPlaygroundController;
import com.rs2.model.gameplay.magetrainingarena.CreatureGraveyardController;
import com.rs2.model.gameplay.magetrainingarena.EnchantmentChamberController;
import com.rs2.model.gameplay.magetrainingarena.TelekineticTheatreController;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.interaction.InteractionDispatcher;
import com.rs2.model.interaction.InteractionType;
import com.rs2.model.item.ItemContainer;
import com.rs2.model.item.ItemContainerType;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.item.action.BarrowsRepairHandler;
import com.rs2.model.item.action.CaveLightSourceDefinition;
import com.rs2.model.item.consumable.FoodHandler;
import com.rs2.model.item.consumable.PotionHandler;
import com.rs2.model.music.MusicManager;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.player.BankManager;
import com.rs2.model.player.BankRearrangeMode;
import com.rs2.model.player.BarrowsChestDamageTask;
import com.rs2.model.player.BotLumbridgeResetTask;
import com.rs2.model.player.CharacterFileRecord;
import com.rs2.model.player.DeathItemValueComparator;
import com.rs2.model.player.DelayedPositionMoveTask;
import com.rs2.model.player.DropGodCapeTask;
import com.rs2.model.player.EmoteManager;
import com.rs2.model.player.EquipmentManager;
import com.rs2.model.player.GrandExchangeManager;
import com.rs2.model.player.HiscoreEntryComparator;
import com.rs2.model.player.InventoryManager;
import com.rs2.model.player.PetManager;
import com.rs2.model.player.PlayerConnectionState;
import com.rs2.model.player.PlayerGroup;
import com.rs2.model.player.PostLoginSyncTask;
import com.rs2.model.player.PostTeleportBotContinuationTask;
import com.rs2.model.player.ProtectedItemValueComparator;
import com.rs2.model.player.RetryMissingNpcSearchTask;
import com.rs2.model.player.RetryMissingObjectSearchTask;
import com.rs2.model.player.RetryUnreachableObjectTask;
import com.rs2.model.player.SocialManager;
import com.rs2.model.player.StartBotCommandTask;
import com.rs2.model.player.TradeState;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.quest.QuestManager;
import com.rs2.model.quest.QuestScript;
import com.rs2.model.quest.impl.ErnestTheChickenQuest;
import com.rs2.model.randomevent.sandwichlady.SandwichLadyManager;
import com.rs2.model.reward.ActionRewardDefinition;
import com.rs2.model.skill.EquipmentKeywordBootstrap;
import com.rs2.model.skill.ItemCombinationHandler;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.SkillManager;
import com.rs2.net.packet.handler.ObjectInteractionPacketHandler;
import com.rs2.model.skill.cooking.CookableFoodDefinition;
import com.rs2.model.skill.cooking.CookingManager;
import com.rs2.model.skill.cooking.DairyChurnHandler;
import com.rs2.model.skill.cooking.WineFermentationHandler;
import com.rs2.model.skill.farming.AllotmentPatchManager;
import com.rs2.model.skill.farming.BushPatchManager;
import com.rs2.model.skill.farming.CompostBinManager;
import com.rs2.model.skill.farming.FarmingToolStore;
import com.rs2.model.skill.farming.FlowerPatchManager;
import com.rs2.model.skill.farming.FruitTreePatchManager;
import com.rs2.model.skill.farming.HerbPatchManager;
import com.rs2.model.skill.farming.HopsPatchManager;
import com.rs2.model.skill.farming.PlantPotHandler;
import com.rs2.model.skill.farming.SpecialCropPatchManager;
import com.rs2.model.skill.farming.SpecialTreePatchManager;
import com.rs2.model.skill.farming.TreePatchManager;
import com.rs2.model.skill.firemaking.FiremakingHandler;
import com.rs2.model.skill.fishing.FishingHandler;
import com.rs2.model.skill.guide.SkillGuideManager;
import com.rs2.model.skill.magic.MagicSpellAction;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.model.skill.magic.Spellbook;
import com.rs2.model.skill.magic.TeleportManager;
import com.rs2.model.skill.mining.MiningManager;
import com.rs2.model.skill.prayer.BoneBuryingHandler;
import com.rs2.model.skill.prayer.PrayerManager;
import com.rs2.model.skill.runecrafting.RunecraftingObjectHandler;
import com.rs2.model.skill.slayer.SlayerManager;
import com.rs2.model.skill.smithing.SmeltingHandler;
import com.rs2.model.skill.smithing.SmithingBarDefinition;
import com.rs2.model.travel.WorldTeleportMenu;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.model.task.TickTask;
import com.rs2.net.DedicatedReactor;
import com.rs2.net.IsaacCipher;
import com.rs2.net.LoginProtocol;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.PacketDispatcher;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.PacketReader;
import com.rs2.net.packet.PacketSender;
import com.rs2.net.packet.PacketWriter;
import com.rs2.net.packet.InitialVarps;
import com.rs2.util.CharacterFileManager;
import com.rs2.util.ChatTextCodec;
import com.rs2.util.ChatCodec;
import com.rs2.util.ElapsedTimer;
import com.rs2.util.FileUtil;
import com.rs2.util.GameUtil;
import com.rs2.util.GameplayTrace;
import com.rs2.util.RectangularArea;
import com.rs2.util.TextUtil;
import com.rs2.util.path.PathFinder;
import com.rs2.util.plugin.PlayerPlugin;
import com.rs2.util.plugin.PluginManager;
import java.awt.Color;
import java.awt.Polygon;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Player
extends Entity {
    public int movementSystemMode = ServerSettings.defaultMovementSystem;
    public int localX = 0;
    public int localY = 0;
    public int currentLevelUpSkillId = -1;
    public boolean deferLevelUpInterfaces = false;
    public ArrayList queuedLevelUpSkillIds = new ArrayList();
    public int reservedSaveByte = 0;
    public int[] godWarsKillCounts = new int[4];
    public int[] lastDisplayedGodWarsKillCounts = new int[4];
    public long godWarsLastAltarBlessingMillis;
    public long dragonfireShieldLastOperateMillis = -1L;
    public PlayerGroup currentGroup;
    public PlayerGroup pendingGroupCleanup;
    public Player pendingGroupInviteTarget;
    public boolean infiniteRunEnabled = false;
    public int godBookPageFlags;
    public int groupLootRoll = 0;
    public int craftingThreadUseCount = 0;
    public int gameMode = 0;
    public Position cutsceneReturnPosition;
    public ArrayList temporaryCutsceneNpcs = new ArrayList();
    private int combatLevel;
    private final SelectionKey selectionKey;
    private final ByteBuffer inboundBuffer;
    private ByteBuffer outboundBuffer;
    private SocketChannel socketChannel;
    private PlayerConnectionState connectionState = PlayerConnectionState.HANDSHAKE;
    private IsaacCipher outboundCipher;
    private IsaacCipher inboundCipher;
    private int currentPacketOpcode = -1;
    private int currentPacketLength = -1;
    private String username;
    private String password;
    private String submittedPassword;
    private int clientBuild;
    private int bossPetUnlockFlags = 0;
    private int loginMagicByte;
    private int openInterfaceId = -1;
    public MagicSpellAction activeMagicSpellAction;
    private final ElapsedTimer packetReadTimer = new ElapsedTimer();
    private final List localPlayers = new LinkedList();
    private final List localNpcs = new LinkedList();
    private InventoryManager inventoryManager = new InventoryManager(this);
    private EquipmentManager equipmentManager = new EquipmentManager(this);
    private SocialManager socialManager = new SocialManager(this);
    private PrayerManager prayerManager = new PrayerManager(this);
    private TeleportManager teleportManager = new TeleportManager(this);
    private EmoteManager emoteManager = new EmoteManager(this);
    private SkillManager skillManager = new SkillManager(this);
    private QuestManager questManager = new QuestManager(this);
    private RunecraftingObjectHandler runecraftingObjectHandler = new RunecraftingObjectHandler(this);
    public PacketSender packetSender = new PacketSender(this);
    private SlayerManager slayerManager = new SlayerManager(this);
    private DuelController duelController = new DuelController(this);
    private DuelSession duelSession = new DuelSession(this);
    private FightCaveController fightCaveController = new FightCaveController(this);
    private AlchemistPlaygroundController alchemistPlaygroundController = new AlchemistPlaygroundController(this);
    private CreatureGraveyardController creatureGraveyardController = new CreatureGraveyardController(this);
    private TelekineticTheatreController telekineticTheatreController = new TelekineticTheatreController(this);
    private EnchantmentChamberController enchantmentChamberController = new EnchantmentChamberController(this);
    private DuelInterfaceManager duelInterfaceManager = new DuelInterfaceManager(this);
    private DuelArenaLocationManager duelArenaLocationManager = new DuelArenaLocationManager(this);
    private WineFermentationHandler wineFermentationHandler = new WineFermentationHandler(this);
    private FishingHandler fishingHandler = new FishingHandler(this);
    private ItemCombinationHandler itemCombinationHandler = new ItemCombinationHandler(this);
    private SkillGuideManager skillGuideManager = new SkillGuideManager(this);
    private FoodHandler foodHandler = new FoodHandler(this);
    private PotionHandler potionHandler = new PotionHandler(this);
    private MiningManager miningManager = new MiningManager(this);
    private CookingManager cookingManager = new CookingManager(this);
    private CompostBinManager compostBinManager = new CompostBinManager(this);
    private AllotmentPatchManager allotmentPatchManager = new AllotmentPatchManager(this);
    private FlowerPatchManager flowerPatchManager = new FlowerPatchManager(this);
    private HerbPatchManager herbPatchManager = new HerbPatchManager(this);
    private HopsPatchManager hopsPatchManager = new HopsPatchManager(this);
    private BushPatchManager bushPatchManager = new BushPatchManager(this);
    private PlantPotHandler plantPotHandler = new PlantPotHandler(this);
    private TreePatchManager treePatchManager = new TreePatchManager(this);
    private FruitTreePatchManager fruitTreePatchManager = new FruitTreePatchManager(this);
    private SpecialTreePatchManager specialTreePatchManager = new SpecialTreePatchManager(this);
    private SpecialCropPatchManager specialCropPatchManager = new SpecialCropPatchManager(this);
    private FarmingToolStore farmingToolStore = new FarmingToolStore(this);
    private boolean farmingLoginCatchUp = true;
    private FiremakingHandler firemakingHandler = new FiremakingHandler(this);
    private BoneBuryingHandler boneBuryingHandler = new BoneBuryingHandler(this);
    private PetManager petManager;
    private SandwichLadyManager sandwichLadyManager;
    private DialogueManager dialogueManager;
    private BankPinManager bankPinManager;
    private LoginProtocol loginProtocol;
    private Position lastKnownRegionPosition;
    private int playerRights;
    private boolean memberFlag;
    public boolean treasureTrailNavigationTaught;
    private int publicChatColor;
    private int idlePacketCount;
    private byte[] publicChatPayload;
    private int[] essencePouchAmounts;
    private int gender;
    private final int[] appearanceParts;
    private final int[] appearanceColors;
    private ItemContainer bankContainer;
    private ItemContainer tradeOfferContainer;
    private ItemContainer partyRoomContainer;
    private int interactionSpellButtonId;
    private int interactionTargetX;
    private int interactionTargetY;
    private int interactionTargetPlane;
    private int interactionTargetId;
    private boolean interactionDebugEnabled;
    private int selectedItemId;
    private int selectedItemInterfaceId;
    private int selectedItemSlot;
    private int interactionTargetIndex;
    private boolean bankWithdrawNoteMode;
    private int selectedInterfaceItemId;
    private int selectedInterfaceSlot;
    private int selectedInterfaceId;
    private BankRearrangeMode bankRearrangeMode;
    private int currentShopId;
    private boolean registered;
    private Map combatBonuses;
    private long[] friendsList;
    private long[] ignoreList;
    private int loginResponseCode;
    private TradeState tradeState;
    private int[] queuedLoginItemIds;
    private int[] queuedLoginItemAmounts;
    private int runEnergyRaw;
    private boolean teleporting;
    private boolean teleportPlacementUpdateRequired;
    private boolean appearanceUpdateRequired;
    private int prayerHeadIcon;
    private int skullIcon;
    private int donatorPoints;
    private boolean[] activePrayers;
    private Spellbook spellbook;
    public Spellbook previousSpellbookBeforeNecromancy;
    private boolean autoRetaliate;
    public boolean skulled;
    private boolean actionLocked;
    private int brightness;
    private int mouseButtons;
    private int publicChatEffects;
    private int publicChatAnimation;
    private int splitPrivateChat;
    private int privateChatMode;
    private int publicChatMode;
    private int tradeMode;
    private int acceptAid;
    private int musicVolume;
    private int effectVolume;
    private boolean specialAttackEnabled;
    private int specialEnergy;
    private int ringOfRecoilLife;
    private int ringOfForgingLife;
    private int bindingNecklaceCharge;
    private int fightMode;
    private boolean crystalBowEquipped;
    private boolean ammunitionDropsEnabled;
    private boolean dharokSetEffectActive;
    private boolean ahrimSetEffectActive;
    private boolean karilSetEffectActive;
    private boolean toragSetEffectActive;
    private boolean guthanSetEffectActive;
    private boolean veracSetEffectActive;
    private List playerPlugins;
    private long protectionPrayerDisabledUntil;
    private int currentWalkableInterfaceId;
    private int selectedSmithingBarItemId;
    private int cookingObjectId;
    private SmithingBarDefinition selectedSmithingBarDefinition;
    private int abyssMageNpcId;
    public String interfaceAction;
    public Npc ownedNpc;
    private long nameHash;
    private WeaponProfile weaponProfile;
    private SpecialAttackDefinition specialAttackDefinition;
    private List pvpCombatReferences;
    private SpellDefinition queuedCombatSpell;
    private SpellDefinition autocastSpell;
    private boolean autocastEnabled;
    public ItemStack pendingDialogueItem;
    public Player pendingItemDropTarget;
    public int pendingGameMode;
    public int temporaryActionValue;
    public int sharedActionValue;
    public int fremennikDoorRiddleFirstLetterIndex;
    public int fremennikDoorRiddleSecondLetterIndex;
    public int fremennikDoorRiddleThirdLetterIndex;
    public int fremennikDoorRiddleFourthLetterIndex;
    public int canoeStationIndex;
    public int builtCanoeTypeConfigValue;
    public ItemStack[] clueRequiredItems;
    public int activeBookItemId;
    public int activeBookPageIndex;
    private boolean hideHeldItemsInAppearance;
    private RectangularArea localViewArea;
    private List visibleGroundItems;
    private long muteExpires;
    private long banExpires;
    private boolean[] barrowsKilledBrothers;
    private int barrowsKillCount;
    private int barrowsTargetBrotherIndex;
    private boolean brimhavenOpen;
    private Position teleotherDestination;
    private ItemStack pendingDestroyItem;
    private boolean bankPinReminderShown;
    private Npc activeRandomEventNpc;
    private ItemStack randomEventRequestedItem;
    private int selectedLampSkill;
    private int[] sidebarInterfaceIds;
    public int interactionExitX;
    public int interactionExitY;
    public int interactionExitPlane;
    public int interactionApproachX;
    public int interactionApproachY;
    public int interactionOffsetX;
    public int interactionOffsetY;
    public int interactionObjectSizeX;
    public int interactionObjectSizeY;
    public Position pendingFarmingPatchPosition;
    private boolean visibleToOtherPlayers;
    public boolean actionSucceeded;
    public boolean wildernessEntryAcknowledged;
    public int npcTransformationId;
    public double carriedWeight;
    public double sextantSunAngleDegrees;
    public int sextantSunVerticalOffset;
    public int sextantSunHorizontalOffset;
    public int sextantHorizonRotation;
    public int sextantHorizonVerticalOffset;
    public int treasureTrailStepCount;
    public boolean killedClueAttacker;
    public int activeClueLevel;
    public int activeClueItemId;
    public ItemStack[] sliderPuzzlePieces;
    private int selectedSkillItemId;
    public boolean forcedMovementActive;
    public int currentMusicTrackId = -1;
    private int runAnimationOverride;
    private int standAnimationOverride;
    private int walkAnimationOverride;
    private String hostAddress;
    private int inventoryOverlayInterfaceId;
    private int[] bankPinEntryDigits;
    private long disconnectGraceExpiresAtMillis;
    private int coalTruckCoalCount;
    private Player tradeRequestTarget;
    private Player duelRequestTarget;
    public boolean publicChatUpdatePending;
    public int botMode;
    public boolean dropPartyLeader;
    public boolean dropPartyPretaskComplete;
    public boolean dropPartyFollower;
    public boolean dropPartySentToAssignedDrop;
    public Position dropPartyAssignedDropPosition;
    public int dropPartyPretaskLoopCount;
    public int tradeAdvertAcceptedQuantity;
    public int tradeAdvertOfferPoolIndex;
    public int tradeAdvertQuantityOptionIndex;
    public boolean tradeAdvertInitialOfferPlaced;
    public boolean tradeAdvertScam;
    public boolean tradeAdvertVariableQuantity;
    public int tradeAdvertLastOfferAmount;
    public int tradeAdvertMode;
    public int botAdvertItemId;
    public int tradeAdvertQuantityRemaining;
    public int tradeAdvertUnitPrice;
    public String botPublicChatMessage;
    public int botPublicChatColor;
    public int botPublicChatEffect;
    public Player pendingTradeTarget;
    private int lastBotStallCheckX;
    private int lastBotStallCheckY;
    private int lastBotStallCheckPlane;
    private long lastBotStallCheckExperience;
    public int savedWorldRouteIndex;
    public BotWorldRouteChoice currentWorldRouteChoice;
    public int botTaskItemId;
    public int botSmithingProductItemId;
    public int botShopItemAmount;
    public boolean botUseTaskItemOnTarget;
    public int botShopBuyMode;
    public int botSkillTargetSkillId;
    public int botSkillTargetLevel;
    public int botReservedGoalByte1;
    public int botReservedGoalByte2;
    public int botReservedGoalByte3;
    public int botReservedGoalByte4;
    public int botCompletionItemId;
    public int botCompletionItemAmount;
    public int botSecondaryCompletionItemId;
    public int botReservedGoalInt2;
    public int botReservedGoalInt3;
    public int botReservedGoalInt4;
    public boolean savedWorldRouteReversed;
    public int botElementalSpellIndex;
    public int currentBotTaskTypeId;
    public int currentBotTaskIndex;
    public int deferredBotTaskTypeId;
    public int deferredBotTaskIndex;
    public BotTaskDefinition deferredBotTask;
    public ArrayList botCombatLoadoutItemIds;
    public ArrayList botMeleeLoadoutItemIds;
    public ArrayList botRangedLoadoutItemIds;
    public ArrayList botMagicLoadoutItemIds;
    public ArrayList botShopSellItemIds;
    public int botCombatLoadoutSlotCursor;
    public boolean botLumbridgeResetPending;
    private int botStallSampleCount;
    public int botPvpTeamInviteTicks;
    public Player botPvpPendingTeamTarget;
    public ArrayList botPvpRejectedTeamTargets;
    public ArrayList botPvpTeamRequesters;
    public Player botPvpChatSource;
    public String botPvpChatMessage;
    private int[] questStates;
    public int[] questProgressFlags;
    public int[] questHookStates;
    public int questRandomSeed;
    public ArrayList visibleDynamicObjects;
    public ArrayList pendingDynamicObjectRemovals;
    public int agilityCourseProgress;
    public int gnomeAgilityCourseProgress;
    public int piratesTreasureBananaCrateCount;
    public int hintedNpcIndex;
    private String profileString1;
    private String profileString2;
    public boolean barrowsDoorPuzzleSolved;
    public int activeBarrowsDoorPuzzleIndex;
    public int barrowsRewardPotential;
    public int[] activeBarrowsDoorPuzzleAnswerObjectIds;
    public boolean barrowsChestOpened;
    public int flourMillHopperGrainCount;
    public long reservedSaveLong1;
    public int reservedSaveInt3;
    public int reservedSaveInt2;
    public String reservedVersion11String;
    public long membershipExpiresMillis;
    private boolean expiredMembershipRelocationRequired;
    public boolean ringOfWealthShinePending;
    public String[] playerOptionTextCache;
    public boolean multiwayAreaState;
    public int musicManagerTrackId;
    public int displayedWildernessLevel;
    public int displayedBarrowsKillCount;
    public int gatheringHazardCounter;
    // Tai Bwo Wannai jungle event build-up is session-only and resets on logout.
    public int jungleEventChanceCounter;
    public long lastCharacterSaveMillis;
    public long lastPacketReceivedMillis;
    /** Last ::fps probe received on this connection; only the network thread writes it. */
    public long lastFpsPingReceivedNanos;
    /** When the reactor last finished a game socket read on this player. */
    public long lastGameSocketReadNanos;
    /** When the first opcode byte of the current FPS ping was decoded. */
    public long fpsPingFirstByteNanos;
    public long lastPlayerInputMillis;
    public int familyCrestGauntletItemId;
    public boolean logoutPacketSent;
    public long lastRegionChangeMillis;
    public Entity botLootResumeTarget;
    public ArrayList botLootGroundItems;
    public ArrayList botLootPickupTargets;
    public ArrayList botLootSellGroundItems;
    public ArrayList botLootSellItems;
    public boolean clanWarsBot;
    public int clanWarsTeamId;
    public long botTaskStartTimeMillis;
    public int botTaskDurationMinutes;
    public long botTaskSavedElapsedMillis;
    public boolean botTaskReturnToBankRequested;
    public ItemStack[] botTaskRequiredItems;
    public Position botEscapeLastPosition;
    public int botEscapeStuckTicks;
    public boolean botCombatEscapeActive;
    public boolean botMagicPenaltyGearUnequipped;
    public boolean botAntipoisonAvailable;
    public int botActiveCombatStyle;
    public int botPrimaryCombatStyle;
    public int botSpecialCombatStyle;
    public int botOpponentCombatStyle;
    public int botCombatStyle;
    public int botSpecialAttackEnergyCost;
    public boolean botStrengthPotionDepleted;
    public boolean botFoodDepleted;
    public int botWeaponItemId;
    public int botShieldItemId;
    public int botSpecialWeaponItemId;
    public SpellDefinition botCombatSpell;
    public SpellDefinition botPrimaryAutocastSpell;
    public int botFoodItemId;
    public int botWildernessMaxY;
    public String botCombatState;
    public TickTask botEscapeLogoutTask;
    public TickTask botCombatTickTask;
    public int botMagicGearSwapDelayTicks;
    public int botThreatEscapeDelayTicks;
    public int botPrayerSwitchDelayTicks;
    public int botQueuedPrayerId;
    public int botEatDelayTicks;
    public int botWeaponSwapDelayTicks;
    public boolean isBot;
    public boolean bonesToPeachesUnlocked;
    public int botPathSegmentIndex;
    public BotTaskDefinition currentBotTask;
    public int botTargetNpcId;
    public int botPathWaypointIndex;
    public String botEscapeRouteName;
    public BotRoute currentBotRoute;
    public boolean botRouteActionPending;
    public boolean botRouteTravelPending;
    public ArrayList botInteractionTargetIds;
    public boolean botEnabled;
    public int botInteractionOption;
    public String botTaskState;
    public int pendingCropResurrectionPatchIndex;
    public String pendingCropResurrectionPatchType;
    public int currentBankTab;
    public int gangAffiliation;
    public boolean[] grandExchangeSellOfferFlags;
    public int[] grandExchangeItemIds;
    public int[] grandExchangeQuantities;
    public int[] grandExchangeUnitPrices;
    public boolean[] grandExchangeCancelledFlags;
    public int[] grandExchangeCompletedQuantities;
    public int[] grandExchangeTotalPrices;
    public int[] grandExchangePrimaryCollectAmounts;
    public int[] grandExchangeSecondaryCollectAmounts;
    public boolean[] grandExchangeFinishMessagePending;
    public int selectedGrandExchangeItemId;
    public int selectedGrandExchangeQuantity;
    public int selectedGrandExchangeUnitPrice;
    public int selectedGrandExchangeSlot;
    public int mageArenaFlamesOfZamorakCastsRemaining;
    public int mageArenaClawsOfGuthixCastsRemaining;
    public int mageArenaSaradominStrikeCastsRemaining;
    public int mageArenaProgressStage;
    public int fightCaveWaveIndex;
    public boolean godModeEnabled;
    private ArrayList fightCaveNpcs;
    public boolean loadedCharacterFromBackup;
    public long lastAbuseReportMillis;
    public int legacyQuestPoints;
    public int npcKillCount;
    public int playerKillCount;
    public int deathCount;
    public int easyCluesCompleted;
    public int mediumCluesCompleted;
    public int hardCluesCompleted;
    public int soldItemsValue;
    public int boughtItemsValue;
    public int duelWins;
    public int duelLosses;
    public int barrowsRunsCompleted;
    public int skeletonSkinUnlocked;
    public boolean loginRestrictionExempt;
    public long createdAtMillis;
    public long lastSavedMillis;
    public long homeTeleportAvailableAtMillis;
    public long sessionStartMillis;
    public long totalPlaytimeMillis;
    public String lastLoginHostAddress;
    public boolean loginInitializationComplete;
    public int reservedSaveInt1;
    public boolean cluePuzzleSolved;
    public boolean automaticMusicEnabled;
    public int[] configStates;
    public int activeRecurringEffectId;
    public int activeEnvironmentalHazardId;
    public int hiscorePage;
    public int hiscoreCategory;
    private int missingNpcSearchRetryCount;
    private int botStallRecoveryAttempts;
    public boolean cutsceneActive;
    private boolean suppressTeleportCleanup;
    boolean planeChangeRefreshPending;
    private static String[] allowedUsernameCharacters = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", " ", ".", "+", "-", "="};
    private static String[] blockedHostAddresses = new String[]{"72.91.46.160", "46.116.160.130", "92.9.242.172", "173.197.132.38", "84.248.174.119"};
    public String pendingPublicChatText;
    public int fightCaveSpawnRotation;
    public int prayerDrainAccumulator;
    public int prayerDrainRate;
    public boolean grandExchangeSettlementInProgress;
    private static Polygon ardougneZooMonkeyEnclosure = null;
    public int savedCacheVersion;
    public int enterTheAbyssMiniquestState;
    public boolean swampCaveRopeAttached;
    public boolean lampOilStillFilled;
    public int caveInsectSwarmStage;
    public int swampGasFlareState;

    public final void clearTemporaryCutsceneNpcs() {
        if (this.temporaryCutsceneNpcs.size() > 0) {
            for (Object npcObject : this.temporaryCutsceneNpcs) {
                Npc npc = (Npc)npcObject;
                if (npc == null) continue;
                GameplayHelper.unregisterTemporaryNpc(npc);
            }
        }
        this.temporaryCutsceneNpcs = new ArrayList();
    }

    public final Npc findTemporaryCutsceneNpc(int npcId) {
        if (this.temporaryCutsceneNpcs.size() > 0) {
            for (Object npcObject : this.temporaryCutsceneNpcs) {
                Npc npc = (Npc)npcObject;
                if (npc == null || npc.getNpcId() != npcId) continue;
                return npc;
            }
        }
        return null;
    }

    public final void spawnTenthSquadSigilNpcs(int npcId) {
        Npc npc = new Npc(1412);
        Npc npc2 = new Npc(1426);
        Npc npc3 = new Npc(1414);
        Npc npc4 = new Npc(1416);
        Npc npc5 = new Npc(1418);
        GameplayHelper.spawnRoamingNpcFacingPlayer(this, npc, 2699, 9168, npcId, -1, false, false);
        GameplayHelper.spawnRoamingNpcFacingPlayer(this, npc2, 2697, 9175, npcId, -1, false, false);
        GameplayHelper.spawnRoamingNpcFacingPlayer(this, npc3, 2703, 9168, npcId, -1, false, false);
        GameplayHelper.spawnRoamingNpcFacingPlayer(this, npc4, 2698, 9172, npcId, -1, false, false);
        GameplayHelper.spawnRoamingNpcFacingPlayer(this, npc5, 2697, 9171, npcId, -1, false, false);
        npc.getUpdateState().setGraphic(86, 25);
        npc2.getUpdateState().setGraphic(86, 25);
        npc3.getUpdateState().setGraphic(86, 25);
        npc4.getUpdateState().setGraphic(86, 25);
        npc5.getUpdateState().setGraphic(86, 25);
        this.temporaryCutsceneNpcs.add(npc);
        this.temporaryCutsceneNpcs.add(npc2);
        this.temporaryCutsceneNpcs.add(npc3);
        this.temporaryCutsceneNpcs.add(npc4);
        this.temporaryCutsceneNpcs.add(npc5);
        if (this.questStates[62] == 21 && !this.hasTenthSquadSigilHintProgress()) {
            Player player = this;
            player.packetSender.sendEntityHintIcon(1, npc.getIndex());
            return;
        }
        if (this.questStates[62] == 21 && this.hasTenthSquadSigilHintProgress()) {
            Player player = this;
            player.packetSender.sendEntityHintIcon(1, npc2.getIndex());
        }
    }

    private boolean hasTenthSquadSigilHintProgress() {
        if (this.questProgressFlags[62] == 0) {
            return false;
        }
        return (this.questProgressFlags[62] & GameUtil.bitFlag(16)) != 0;
    }

    public final void addBotTaskRequiredItem(int itemId, int value2) {
        ItemStack itemStack = new ItemStack(itemId, value2);
        ItemStack[] itemStackArray;
        if (this.botTaskRequiredItems != null) {
            itemStackArray = new ItemStack[this.botTaskRequiredItems.length + 1];
            int index = 0;
            while (index < this.botTaskRequiredItems.length) {
                itemStackArray[index] = this.botTaskRequiredItems[index];
                ++index;
            }
            itemStackArray[this.botTaskRequiredItems.length] = itemStack;
        } else {
            itemStackArray = new ItemStack[1];
            itemStackArray[0] = itemStack;
        }
        this.botTaskRequiredItems = itemStackArray;
    }

    private boolean recoverBotTaskStall(boolean enabled2) {
        if (enabled2) {
            Player player = this;
            System.out.println("Detected possibly frozen bot: " + player.username + " at: " + this.getPosition() + ", trying to apply fix.");
        }
        if (this.botTaskState.equals("do task")) {
            if (this.botStallRecoveryAttempts == 0) {
                ++this.botStallRecoveryAttempts;
                this.moveTo(this.currentBotTask.getTaskPosition());
                Player player = this;
                System.out.println(String.valueOf(player.username) + " teleported to task location: " + this.currentBotTask.getTaskPosition());
                this.startCurrentBotTaskInteraction();
            } else {
                Player player = this;
                System.out.println(String.valueOf(player.username) + " can't seem to continue its task.");
                System.out.println("Reseting to lumbridge and picking new task.");
                this.botStallRecoveryAttempts = 0;
                this.resetBotToLumbridge();
            }
            return true;
        }
        if (!BotWorldRouteWalker.findWorldRoute(this)) {
            this.moveTo(this.currentBotTask.getStartPosition());
            Player player = this;
            System.out.println(String.valueOf(player.username) + " teleported to task start location: " + this.currentBotTask.getStartPosition());
            return true;
        }
        return false;
    }

    private void resetBotToLumbridge() {
        this.botLumbridgeResetPending = true;
        Object value = this;
        this.botShopSellItemIds.clear();
        this.botTaskRequiredItems = null;
        this.moveTo(new Position(ServerSettings.respawnX, ServerSettings.respawnY, ServerSettings.respawnPlane));
        World.getTaskScheduler().schedule(new BotLumbridgeResetTask(this, 2, (Player)value));
    }

    public final boolean hasBotStalled() {
        int position = this.getPosition().getX();
        int position2 = this.getPosition().getY();
        int position3 = this.getPosition().getPlane();
        Player player = this;
        long totalExperience = player.skillManager.getTotalExperience();
        if (position == this.lastBotStallCheckX && position2 == this.lastBotStallCheckY && position3 == this.lastBotStallCheckPlane && totalExperience == this.lastBotStallCheckExperience) {
            ++this.botStallSampleCount;
            if (this.botStallSampleCount == 5) {
                return true;
            }
        } else {
            this.botStallSampleCount = 0;
        }
        this.lastBotStallCheckX = position;
        this.lastBotStallCheckY = position2;
        this.lastBotStallCheckPlane = position3;
        this.lastBotStallCheckExperience = totalExperience;
        return false;
    }

    public final void resumeBotTaskState() {
        int value;
        Object value2;
        this.botEnabled = true;
        this.botTaskStartTimeMillis = System.currentTimeMillis();
        Player player = this;
        if (BotPlayer.forceResetBotNames.contains(player.username.toLowerCase())) {
            player = this;
            System.out.println(String.valueOf(player.username) + " was force reseted.");
            this.resetBotToLumbridge();
            return;
        }
        Object value3 = null;
        if (this.currentBotTaskIndex != -1 && this.currentBotTaskTypeId != -1) {
            value2 = BotTaskDefinition.getTaskByTypeAndIndex(this.currentBotTaskTypeId, this.currentBotTaskIndex);
            player = this;
            this.currentBotTask = (BotTaskDefinition)value2;
            if (this.currentBotTask != null) {
                this.currentBotTask.configureTaskInteractionTargets(this);
            }
        }
        if (this.deferredBotTaskIndex != -1 && this.deferredBotTaskTypeId != -1) {
            this.deferredBotTask = BotTaskDefinition.getTaskByTypeAndIndex(this.deferredBotTaskTypeId, this.deferredBotTaskIndex);
        }
        if (this.currentBotTask == null) {
            player = this;
            System.out.println("Reseting " + player.username + " to lumbridge and picking new task");
            if (!this.isBot) {
                player = this;
                player.packetSender.sendGameMessage("You didn't have a bot task to continue.");
                player = this;
                player.packetSender.sendGameMessage("You have been reset to Lumbridge and given new task.");
            }
            this.resetBotToLumbridge();
            return;
        }
        if (this.currentBotTask.taskRouteSegments != null && this.botPathSegmentIndex > this.currentBotTask.taskRouteSegments.length - 1) {
            player = this;
            System.out.println(String.valueOf(player.username) + " has bugged task path at: " + this.getPosition());
            System.out.println("Reseting to lumbridge and picking new task");
            this.resetBotToLumbridge();
            return;
        }
        if (this.savedWorldRouteIndex != -1) {
            value2 = BotWorldRoute.values()[this.savedWorldRouteIndex];
            value3 = new BotWorldRouteChoice((BotWorldRoute)((Object)value2), this.savedWorldRouteReversed);
        }
        if (this.botAdvertItemId != -1) {
            BotTradeAdvertManager.updateTradeAdvertMessage(this);
            CacheArchiveEntry.startTradeOfferTick(this);
        }
        if (this.currentBotTask.membersOnly && ServerSettings.freeToPlayWorld) {
            this.resetBotToLumbridge();
            return;
        }
        boolean enabled = true;
        if (!this.currentBotTask.isAvailableFor(this, true)) {
            player = this;
            System.out.println("Detected possibly bugged bot: " + player.username + " at: " + this.getPosition() + ", trying to apply fix.");
            enabled = false;
            this.botTaskReturnToBankRequested = true;
            this.currentBotTask.startWalkToBank(this);
        }
        if (this.currentBotTaskTypeId == 3 && (this.botTaskRequiredItems == null || this.botTaskRequiredItems.length == 0)) {
            this.botTaskRequiredItems = new ItemStack[]{new ItemStack(this.botTaskItemId, 28)};
        }
        if (this.currentBotTaskTypeId == 5 && (this.botTaskRequiredItems == null || this.botTaskRequiredItems.length == 0) && this.botTaskItemId > 0) {
            SmeltingHandler.prepareBotSmeltingRequirements(this, this.botTaskItemId);
        }
        if (this.botTaskState.equals("do task") && this.currentBotTaskTypeId != 14) {
            int distance = GameUtil.getDistance(this.getPosition(), this.currentBotTask.getTaskPosition());
            if (distance >= 60 && !this.recoverBotTaskStall(enabled)) {
                return;
            }
            if (this.currentBotTaskTypeId == 3) {
                Player player2 = this;
                if (player2.inventoryManager.getItemAmount(this.botTaskItemId) == 0) {
                    value3 = CookableFoodDefinition.forRawItemId(this.botTaskItemId);
                    if (value3 != null) {
                        player2 = this;
                        if (player2.inventoryManager.getContainer().containsItem(((CookableFoodDefinition)((Object)value3)).getBurntItemId())) {
                            player2 = this;
                            ItemStack[] itemStackArray = player2.inventoryManager.getContainer().getItems();
                            int length = itemStackArray.length;
                            int index = 0;
                            while (index < length) {
                                ItemStack itemStack = itemStackArray[index];
                                if (itemStack != null && itemStack.getId() == ((CookableFoodDefinition)((Object)value3)).getBurntItemId()) {
                                    BotCombatHelper.dropInventoryItem(this, itemStack);
                                }
                                ++index;
                            }
                        }
                    }
                    this.currentBotTask.startWalkToBank(this);
                    return;
                }
            }
            if (!(this.currentBotTaskTypeId != 3 && this.currentBotTaskTypeId != 6 && this.currentBotTaskTypeId != 11 || this.botUseTaskItemOnTarget)) {
                this.botUseTaskItemOnTarget = true;
            }
        }
        if (this.currentBotTask == null) {
            System.out.println("Resetting " + this.username
                    + " because its bot task disappeared while resuming state.");
            this.resetBotToLumbridge();
            return;
        }
        if (this.botTaskState.equals("empty inventory") && this.currentBotTaskTypeId != 14 && (value = GameUtil.getDistance(this.getPosition(), this.currentBotTask.getStartPosition())) >= 60 && !this.recoverBotTaskStall(enabled)) {
            return;
        }
        if (this.currentBotTask == null) {
            System.out.println("Resetting " + this.username
                    + " because its bot task disappeared during route recovery.");
            this.resetBotToLumbridge();
            return;
        }
        if (!(this.botTaskState.equals("walk to task") || this.botTaskState.equals("walk to bank") || this.botTaskState.equals("walk towards task"))) {
        }
        if (this.botTaskState.equals("walk to bank")) {
            this.currentBotTask.continueWalkToBank(this, this.botPathWaypointIndex);
            return;
        }
        if (this.botTaskState.equals("worldwalk to bank")) {
            GameplayHelper.startBotTaskRoute(this);
            return;
        }
        if (this.botTaskState.equals("walk towards bank")) {
            this.currentBotTask.continueWalkToBank(this, this.botPathWaypointIndex);
            return;
        }
        if (this.botTaskState.equals("walk to task")) {
            this.currentBotTask.continueWalkToTask(this, this.botPathWaypointIndex);
            return;
        }
        if (this.botTaskState.equals("walk towards task")) {
            this.currentBotTask.continueWalkToTask(this, this.botPathWaypointIndex);
            return;
        }
        if (this.botTaskState.equals("world walk find")) {
            if (value3 == null) {
                Player player3 = this;
                System.out.println(String.valueOf(player3.username) + " is trying to continue path while not having one at: " + this.getPosition());
                System.out.println("Reseting to lumbridge and picking new task");
                this.resetBotToLumbridge();
                return;
            }
            BotWorldRouteWalker.continueWorldRoute(this, (BotWorldRouteChoice)value3);
            return;
        }
        if (this.botTaskState.equals("world walk towards")) {
            if (value3 == null) {
                Player player4 = this;
                System.out.println(String.valueOf(player4.username) + " is trying to continue path while not having one at: " + this.getPosition());
                System.out.println("Reseting to lumbridge and picking new task");
                this.resetBotToLumbridge();
                return;
            }
            BotWorldRouteWalker.continueWorldRoute(this, (BotWorldRouteChoice)value3);
            return;
        }
        if (this.botTaskState.equals("world walk finish")) {
            if (value3 == null) {
                Player player5 = this;
                System.out.println(String.valueOf(player5.username) + " is trying to continue path while not having one at: " + this.getPosition());
                System.out.println("Reseting to lumbridge and picking new task");
                this.resetBotToLumbridge();
                return;
            }
            BotWorldRouteWalker.continueWorldRoute(this, (BotWorldRouteChoice)value3);
            return;
        }
        if (this.botTaskState.equals("do task")) {
            if (this.currentBotTaskTypeId == 14) {
                this.currentBotTask.startWalkToTask(this);
                return;
            }
            this.botInteractionOption = this.currentBotTask.getInteractionOption(this);
            if (!this.dropPartyLeader && !this.currentBotTask.usesCustomTaskAction) {
                int value4;
                if (this.botInteractionTargetIds.size() == 1 && (value4 = ((Integer)this.botInteractionTargetIds.get(0)).intValue()) == -1 && !this.recoverBotTaskStall(enabled)) {
                    return;
                }
                if (this.currentBotTask.interactionTargetType == 0) {
                    this.interactWithBotObjectTargets(this.botInteractionTargetIds);
                } else {
                    this.interactWithBotNpcTargets(this.botInteractionTargetIds);
                }
            }
            if (this.currentBotTask.usesCustomTaskAction) {
                this.currentBotTask.startCustomTaskAction(this);
            }
            return;
        }
        if (this.botTaskState.equals("empty inventory")) {
            if (this.currentBotTask.usesDepositBox) {
                this.botInteractionOption = 2;
                ArrayList<Integer> arrayList = new ArrayList<Integer>();
                arrayList.add(2619);
                this.interactWithBotNpcTargets(arrayList);
                return;
            }
            this.botInteractionOption = 2;
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            arrayList.add(2213);
            arrayList.add(11758);
            this.interactWithBotObjectTargets(arrayList);
            return;
        }
    }

    public final void queuePublicChatMessage(String message, int publicChatColor, int publicChatEffects) {
        if (message == null) {
            return;
        }
        if (message.equals("")) {
            return;
        }
        if (message.length() > 80) {
            message = message.substring(0, 80);
        }
        byte[] payload;
        if (ServerSettings.clientBuild == 443) {
            // Queued bot/server chat must use the same wordpack as player chat.
            payload = ChatCodec.get().encode(message);
        } else {
            byte[] legacy = new byte[100];
            int length = ChatTextCodec.encode(message, legacy);
            payload = Arrays.copyOf(legacy, length);
        }
        this.publicChatAnimation = publicChatEffects;
        this.publicChatColor = publicChatColor;
        this.publicChatPayload = payload;
        this.publicChatUpdatePending = true;
        this.flagAppearanceUpdate(true);
        this.getUpdateState().setUpdateRequired(true);
    }

    public final void queuePublicChatMessage(String publicChatMessage) {
        this.queuePublicChatMessage(publicChatMessage, 0, 0);
    }

    public final void resetAnimation() {
        this.getUpdateState().setAnimation(-1);
    }

    public final int getQuestState(int questId) {
        return this.getQuestState(questId, false);
    }

    private static final int TUTORIAL_EMOTE_COMPLETED_FLAG = 1;
    private static final int TUTORIAL_RUN_SETTINGS_OPENED_FLAG = 2;
    private static final int TUTORIAL_EQUIPMENT_STATS_OPENED_FLAG = 4;

    public final boolean isTutorialEmoteCompleted() {
        return (this.questProgressFlags[0] & TUTORIAL_EMOTE_COMPLETED_FLAG) != 0;
    }

    public final void setTutorialEmoteCompleted() {
        this.questProgressFlags[0] |= TUTORIAL_EMOTE_COMPLETED_FLAG;
    }

    public final boolean isTutorialRunSettingsOpened() {
        return (this.questProgressFlags[0] & TUTORIAL_RUN_SETTINGS_OPENED_FLAG) != 0;
    }

    public final void setTutorialRunSettingsOpened() {
        this.questProgressFlags[0] |= TUTORIAL_RUN_SETTINGS_OPENED_FLAG;
    }

    public final boolean isTutorialEquipmentStatsOpened() {
        return (this.questProgressFlags[0] & TUTORIAL_EQUIPMENT_STATS_OPENED_FLAG) != 0;
    }

    public final void setTutorialEquipmentStatsOpened() {
        this.questProgressFlags[0] |= TUTORIAL_EQUIPMENT_STATS_OPENED_FLAG;
    }

    public final boolean isTutorialRunUnlocked() {
        int tutorialStage = this.getQuestState(0);
        if (tutorialStage == 1 || tutorialStage > 23) {
            return true;
        }
        return tutorialStage == 23 && this.isTutorialRunSettingsOpened();
    }

    public final int getQuestState(int questId, boolean questId2) {
        if (!ServerSettings.skipRequirementsForMissingQuests || questId2) {
            return this.questStates[questId];
        }
        QuestScript questScript = QuestDefinition.getQuestScript(questId);
        if (questScript.getQuestId() == -1) {
            return 1;
        }
        return this.questStates[questId];
    }

    public final void setQuestState(int questId, int questState) {
        int value = this.questStates[questId];
        this.questStates[questId] = questState;
        if (GameplayTrace.enabled() && !this.isBot && value != questState) {
            GameplayTrace.log("quest state set player=" + GameplayTrace.describe(this) + " questId=" + questId + " oldState=" + value + " newState=" + questState);
        }
    }

    public final void addQuestState(int questId, int questState) {
        int value = questId;
        int value2 = this.questStates[value];
        this.questStates[value] = this.questStates[value] + questState;
        if (GameplayTrace.enabled() && !this.isBot && value2 != this.questStates[value]) {
            GameplayTrace.log("quest state add player=" + GameplayTrace.describe(this) + " questId=" + questId + " delta=" + questState + " oldState=" + value2 + " newState=" + this.questStates[value]);
        }
    }

    public static ActionRewardDefinition rollActionReward() {
        return null;
    }

    public final boolean isMember() {
        Player player;
        isMemberControlExit1: {
            isMemberControlExit2: {
                if (this.isBot) {
                    return true;
                }
                if (ServerSettings.membershipRequirementMode == 0) {
                    return false;
                }
                if (ServerSettings.membershipRequirementMode == 1) {
                    return true;
                }
                player = this;
                if (player.playerRights >= 2) break isMemberControlExit2;
                player = this;
                if (!player.memberFlag || ServerSettings.membershipDaysPerPurchase > 0) break isMemberControlExit1;
            }
            return true;
        }
        if (ServerSettings.membershipRequirementMode == 2) {
            player = this;
            if (player.skillManager.getTotalLevel() >= ServerSettings.membershipRequirementValue) {
                return true;
            }
        }
        if (ServerSettings.membershipRequirementMode == 3 && this.getQuestPoints() >= ServerSettings.membershipRequirementValue) {
            return true;
        }
        return ServerSettings.membershipDaysPerPurchase > 0 && this.getMembershipDaysRemaining() > 0;
    }

    public final int getMembershipDaysRemaining() {
        getMembershipDaysRemainingControlExit1: {
            getMembershipDaysRemainingControlExit2: {
                getMembershipDaysRemainingControlExit3: {
                    if (ServerSettings.membershipRequirementMode == 1) break getMembershipDaysRemainingControlExit2;
                    Player player = this;
                    if (player.playerRights >= 2) break getMembershipDaysRemainingControlExit2;
                    player = this;
                    if (player.memberFlag && ServerSettings.membershipDaysPerPurchase <= 0) break getMembershipDaysRemainingControlExit2;
                    if (ServerSettings.membershipRequirementMode != 2) break getMembershipDaysRemainingControlExit3;
                    player = this;
                    if (player.skillManager.getTotalLevel() >= ServerSettings.membershipRequirementValue) break getMembershipDaysRemainingControlExit2;
                }
                if (ServerSettings.membershipRequirementMode != 3 || this.getQuestPoints() < ServerSettings.membershipRequirementValue) break getMembershipDaysRemainingControlExit1;
            }
            return 365;
        }
        if (this.membershipExpiresMillis == 0L) {
            return 0;
        }
        int daysBetweenMidnights = GameplayHelper.getDaysBetweenMidnights(System.currentTimeMillis(), this.membershipExpiresMillis);
        if (daysBetweenMidnights < 0) {
            daysBetweenMidnights = 0;
            this.expiredMembershipRelocationRequired = true;
        }
        return daysBetweenMidnights;
    }

    public final long getBotTaskRuntimeMillis() {
        return System.currentTimeMillis() - this.botTaskStartTimeMillis;
    }

    public final void setPendingCropResurrectionTarget(String pendingCropResurrectionPatchType, int pendingCropResurrectionPatchIndex) {
        this.pendingCropResurrectionPatchType = pendingCropResurrectionPatchType;
        this.pendingCropResurrectionPatchIndex = pendingCropResurrectionPatchIndex;
    }

    public final void clearPendingCropResurrectionTarget() {
        this.pendingCropResurrectionPatchType = "";
        this.pendingCropResurrectionPatchIndex = -1;
    }

    public final ArrayList getFightCaveNpcs() {
        return this.fightCaveNpcs;
    }

    public final void clearFightCaveNpcs() {
        this.fightCaveNpcs.clear();
    }

    public final void addFightCaveNpc(Npc npc) {
        this.fightCaveNpcs.add(npc);
    }

    public final void removeFightCaveNpc(Npc npc) {
        if (this.fightCaveNpcs.contains(npc)) {
            this.fightCaveNpcs.remove(npc);
        }
        this.packetSender.sendGameMessage("Enemies left: " + this.fightCaveNpcs.size());
    }

    public final int getTemporaryActionValue() {
        return this.temporaryActionValue;
    }

    public final void setTemporaryActionValue(int temporaryActionValue) {
        this.temporaryActionValue = temporaryActionValue;
    }

    public final int getPrivateChatMode() {
        return this.privateChatMode;
    }

    public final void setPrivateChatMode(int privateChatMode) {
        this.privateChatMode = privateChatMode;
    }

    public final int getPublicChatMode() {
        return this.publicChatMode;
    }

    public final void setPublicChatMode(int publicChatMode) {
        this.publicChatMode = publicChatMode;
    }

    public final int getTradeMode() {
        return this.tradeMode;
    }

    public final void setTradeMode(int tradeMode) {
        this.tradeMode = tradeMode;
    }

    public final int getMaxedSkillCount() {
        int index = 0;
        int index2 = 0;
        while (index2 < 22) {
            Player player = this;
            int baseLevel = player.skillManager.getBaseLevel(index2);
            if (baseLevel == 99) {
                ++index;
            }
            ++index2;
        }
        return index;
    }

    private void clearStatsInterfaceText() {
        Player player = this;
        player.packetSender.sendInterfaceText("", 8144);
        player = this;
        player.packetSender.sendInterfaceText("", 8145);
        int value = 8147;
        while (value <= 8195) {
            player = this;
            player.packetSender.sendInterfaceText("", value);
            ++value;
        }
        value = 12174;
        while (value <= 12223) {
            player = this;
            player.packetSender.sendInterfaceText("", value);
            ++value;
        }
    }

    private void sendStatsInterfaceLines(String[] interfaceId) {
        Player player = this;
        player.packetSender.sendInterfaceText(interfaceId[0], 8145);
        int initialValue = 1;
        while (initialValue < 23) {
            player = this;
            player.packetSender.sendInterfaceText(interfaceId[initialValue], initialValue + 8146);
            ++initialValue;
        }
    }

    private long getSessionPlaytimeMillis() {
        return System.currentTimeMillis() - this.sessionStartMillis;
    }

    public final long getTotalPlaytimeMillis() {
        return this.totalPlaytimeMillis + this.getSessionPlaytimeMillis();
    }

    public final boolean hasMageArenaGodCape() {
        return this.ownsItem(2412) || this.ownsItem(2413) || this.ownsItem(2414);
    }

    public final void startGodCapeDrop(Player player2, int value2) {
        boolean enabled = true;
        Player player = player2;
        ((Player)player2).actionLocked = enabled;
        World.getTaskScheduler().schedule(new DropGodCapeTask(this, 4, (Player)player2, value2));
    }

    public final void process() {
        this.pruneExpiredDamageContributions();
        if (this.pvpCombatReferences.size() != 0) {
            Iterator iterator = this.pvpCombatReferences.iterator();
            while (iterator.hasNext()) {
                PvpCombatReference pvpCombatReference = (PvpCombatReference)iterator.next();
                if (!pvpCombatReference.hasExpired()) continue;
                iterator.remove();
            }
            if (this.pvpCombatReferences.size() == 0) {
                this.setSkulled(false);
            }
        }
        this.prayerManager.drainPrayerPoints();
        this.skillManager.startRestorationTasks();
        Iterator iterator = this.playerPlugins.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        if (!this.botEnabled && this.idlePacketCount <= 20 && (this.getSingleCombatTimer().hasElapsed() || this.isInMultiCombatArea())) {
            boolean mageArenaTargetActive = this.ownedNpc != null && this.mageArenaProgressStage < 5 && this.ownedNpc.getNpcId() >= 907 && this.ownedNpc.getNpcId() <= 911;
            if (!mageArenaTargetActive && (this.equipmentManager.getItemIdAtSlot(3) != 4024 || !this.isInApeAtoll())) {
                for (Object localNpcObject : this.localNpcs) {
                    Npc npc = (Npc)localNpcObject;
                    if (npc.getOwnerPlayer() != null) continue;
                    if (npc.getDefinition().getAggressionType() != 3 && this.lastRegionChangeMillis != 0L && !npc.isInWilderness() && System.currentTimeMillis() - this.lastRegionChangeMillis > 900000L) break;
                    boolean aggressive;
                    if (npc.hasCombatTarget() || !npc.getDefinition().isAttackable()) {
                        aggressive = false;
                    } else if (npc.getNpcId() == 2429 || npc.getNpcId() == 1827 || npc.getNpcId() == 1266 || npc.getNpcId() == 1268 || npc.getNpcId() == 2453 || npc.getNpcId() == 2890) {
                        aggressive = true;
                    } else if (npc.getNpcId() == 18) {
                        aggressive = this.getCombatTarget() != null;
                    } else if (npc.isInWilderness() || npc.getDefinition().getAggressionType() >= 2) {
                        aggressive = true;
                    } else if (npc.getDefinition().getAggressionType() == 0) {
                        aggressive = false;
                    } else if (npc.getDefinition().getAggressionType() == 1 && this.combatLevel > npc.getDefinition().getCombatLevel() << 1) {
                        aggressive = false;
                    } else {
                        aggressive = true;
                    }
                    if (!aggressive || !GameUtil.hasClearPath(this.getPosition(), npc.getPosition(), false)) continue;
                    int aggressionRange = npc.getDefinition().getAggressionRange();
                    if (ServerSettings.content2007Enabled && (npc.getNpcId() != 6203 && npc.getNpcId() != 6204 && npc.getNpcId() != 6206 && npc.getNpcId() != 6208 && GodWarsDungeonManager.zamorakNpcIds.contains(npc.getNpcId()) && (this.hasEquippedItemNamePrefix("zamorak") || this.hasEquippedItemNamePrefix("unholy")) || npc.getNpcId() != 6247 && npc.getNpcId() != 6248 && npc.getNpcId() != 6250 && npc.getNpcId() != 6252 && GodWarsDungeonManager.saradominNpcIds.contains(npc.getNpcId()) && (this.hasEquippedItemNamePrefix("saradomin") || this.hasEquippedItemNamePrefix("holy")) || npc.getNpcId() != 6222 && npc.getNpcId() != 6223 && npc.getNpcId() != 6225 && npc.getNpcId() != 6227 && GodWarsDungeonManager.armadylNpcIds.contains(npc.getNpcId()) && this.hasEquippedItemNamePrefix("armadyl") || npc.getNpcId() != 6260 && npc.getNpcId() != 6261 && npc.getNpcId() != 6263 && npc.getNpcId() != 6265 && GodWarsDungeonManager.bandosNpcIds.contains(npc.getNpcId()) && this.hasEquippedItemNamePrefix("bandos"))) continue;
                    if (npc.getNpcId() == 912 && this.equipmentManager.getItemIdAtSlot(1) == 2414) {
                        if (GameUtil.isWithinDistance(npc.getSpawnPosition(), this.getPosition(), aggressionRange) && GameUtil.randomInt(4) == 0) {
                            npc.getUpdateState().setForcedText("Hail Zamorak!");
                        }
                        continue;
                    }
                    if (npc.getNpcId() == 913 && this.equipmentManager.getItemIdAtSlot(1) == 2412) {
                        if (GameUtil.isWithinDistance(npc.getSpawnPosition(), this.getPosition(), aggressionRange) && GameUtil.randomInt(4) == 0) {
                            npc.getUpdateState().setForcedText("Hail Saradomin!");
                        }
                        continue;
                    }
                    if (npc.getNpcId() == 914 && this.equipmentManager.getItemIdAtSlot(1) == 2413) {
                        if (GameUtil.isWithinDistance(npc.getSpawnPosition(), this.getPosition(), aggressionRange) && GameUtil.randomInt(4) == 0) {
                            npc.getUpdateState().setForcedText("Hail Guthix!");
                        }
                        continue;
                    }
                    if (npc.getNpcId() == 1266 || npc.getNpcId() == 1268 || npc.getNpcId() == 2453 || npc.getNpcId() == 2890) {
                        aggressionRange = 1;
                    }
                    if (npc.getNpcId() == 2894 || npc.getNpcId() == 2896) {
                        aggressionRange = 10;
                    }
                    if (!GameUtil.isWithinDistance(npc.getSpawnPosition(), this.getPosition(), aggressionRange) || CombatCycleEvent.validateAttack((Entity)npc, this) != AttackValidationResult.VALID) continue;
                    if (npc.getNpcId() == 180) {
                        npc.getUpdateState().setForcedText("Stand and deliver!");
                    }
                    if (npc.getNpcId() == 18) {
                        npc.getUpdateState().setForcedText("Brother, I will help thee with this infidel!");
                    }
                    if (npc.getTransformTicksRemaining() <= 0 && npc.getCombatTransformNpcId() > 0) {
                        npc.transformToNpcId(npc.getCombatTransformNpcId(), 999999);
                    }
                    CombatManager.startCombat((Entity)npc, this);
                    break;
                }
            }
        }
        this.getTargetMovement().process();
        this.processConnectionTimeout();
    }

    /** Called once per world tick after movement, for every client revision. */
    public final void restoreRunEnergy() {
        // The run toggle and a following target do not describe movement that
        // actually happened. A second step does, including the final run step.
        if (this.getRunDirection() != -1 || this.forcedMovementActive
                || this.getRunEnergyRaw() >= 10000) {
            return;
        }
        int agilityLevel = ServerSettings.freeToPlayWorld
                ? 1 : this.skillManager.getCurrentLevels()[16];
        // Pre-rework recovery: 100 raw units = 1%, one tick = 0.6 seconds.
        int previousPercent = this.getRunEnergyPercent();
        this.addRunEnergyRaw(8 + agilityLevel / 6);
        if (this.getRunEnergyPercent() != previousPercent) {
            this.packetSender.sendRunEnergy();
        }
    }

    public final void processConnectionTimeout() {
        if (this.isBot || this.getConnectionState() != PlayerConnectionState.IN_GAME) {
            return;
        }
        long now = System.currentTimeMillis();
        // A dead connection must leave the world even if combat keeps refreshing its timer.
        if (this.lastPacketReceivedMillis != 0L && now - this.lastPacketReceivedMillis >= 60000L) {
            this.disconnect();
        } else if (false && ServerSettings.idleLogoutEnabled && this.getPlayerRights() < 2
                && this.lastPlayerInputMillis != 0L && now - this.lastPlayerInputMillis >= 300000L
                && this.getSingleCombatTimer().hasElapsed()) {
            this.packetSender.sendLogout();
            this.disconnect();
        }
    }

    public Player(SelectionKey selectionKey) {
        new EquipmentKeywordBootstrap(this);
        new DairyChurnHandler(this);
        this.petManager = new PetManager(this);
        this.sandwichLadyManager = new SandwichLadyManager(this);
        this.dialogueManager = new DialogueManager(this);
        this.bankPinManager = new BankPinManager(this);
        this.loginProtocol = new LoginProtocol();
        this.lastKnownRegionPosition = new Position(0, 0, 0);
        this.playerRights = 0;
        this.treasureTrailNavigationTaught = false;
        this.essencePouchAmounts = new int[4];
        this.gender = 0;
        this.appearanceParts = new int[7];
        this.appearanceColors = new int[5];
        this.bankContainer = new ItemContainer(ItemContainerType.b, 288, 10);
        this.tradeOfferContainer = new ItemContainer(ItemContainerType.a, 28);
        this.partyRoomContainer = new ItemContainer(ItemContainerType.a, 8);
        this.interactionSpellButtonId = -1;
        this.interactionTargetX = -1;
        this.interactionTargetY = -1;
        this.interactionTargetPlane = -1;
        this.interactionTargetId = -1;
        this.selectedItemId = -1;
        this.selectedItemInterfaceId = -1;
        this.selectedItemSlot = -1;
        this.bankRearrangeMode = BankRearrangeMode.SWAP;
        this.combatBonuses = new HashMap();
        this.friendsList = new long[200];
        this.ignoreList = new long[100];
        this.loginResponseCode = 2;
        this.tradeState = TradeState.NONE;
        this.queuedLoginItemIds = new int[28];
        this.queuedLoginItemAmounts = new int[28];
        this.runEnergyRaw = 10000;
        this.prayerHeadIcon = -1;
        this.skullIcon = -1;
        this.donatorPoints = 0;
        this.activePrayers = new boolean[24];
        this.spellbook = Spellbook.MODERN;
        this.previousSpellbookBeforeNecromancy = Spellbook.MODERN;
        this.autoRetaliate = false;
        this.brightness = 2;
        this.mouseButtons = 0;
        this.publicChatEffects = 1;
        this.splitPrivateChat = 0;
        this.privateChatMode = 0;
        this.publicChatMode = 0;
        this.tradeMode = 0;
        this.acceptAid = 0;
        this.musicVolume = 0;
        this.effectVolume = 0;
        this.specialAttackEnabled = false;
        this.specialEnergy = 100;
        this.ringOfRecoilLife = 40;
        this.ringOfForgingLife = 140;
        this.bindingNecklaceCharge = 15;
        this.playerPlugins = new ArrayList();
        this.abyssMageNpcId = 553;
        new ArrayList();
        this.interfaceAction = "";
        this.weaponProfile = WeaponProfile.FISTS;
        this.autocastEnabled = false;
        this.visibleGroundItems = new LinkedList();
        this.barrowsKilledBrothers = new boolean[6];
        int[] integerValues = new int[14];
        integerValues[0] = 2423;
        integerValues[1] = 3917;
        integerValues[2] = 638;
        integerValues[3] = 3213;
        integerValues[4] = 1644;
        integerValues[5] = 5608;
        integerValues[7] = -1;
        integerValues[8] = 5065;
        integerValues[9] = 5715;
        integerValues[10] = 2449;
        integerValues[11] = 904;
        integerValues[12] = 147;
        integerValues[13] = 962;
        this.sidebarInterfaceIds = integerValues;
        this.visibleToOtherPlayers = true;
        this.actionSucceeded = false;
        this.wildernessEntryAcknowledged = false;
        this.npcTransformationId = -1;
        this.sliderPuzzlePieces = new ItemStack[25];
        this.forcedMovementActive = false;
        this.runAnimationOverride = -1;
        this.standAnimationOverride = -1;
        this.walkAnimationOverride = -1;
        this.bankPinEntryDigits = new int[4];
        this.publicChatUpdatePending = false;
        this.botMode = -1;
        this.dropPartyLeader = false;
        this.dropPartyPretaskComplete = false;
        this.dropPartyFollower = false;
        this.dropPartySentToAssignedDrop = false;
        this.dropPartyPretaskLoopCount = 0;
        this.tradeAdvertAcceptedQuantity = -1;
        this.tradeAdvertOfferPoolIndex = -1;
        this.tradeAdvertQuantityOptionIndex = -1;
        this.tradeAdvertInitialOfferPlaced = false;
        this.tradeAdvertScam = false;
        this.tradeAdvertVariableQuantity = false;
        this.tradeAdvertLastOfferAmount = -1;
        this.tradeAdvertMode = -1;
        this.botAdvertItemId = -1;
        this.tradeAdvertQuantityRemaining = -1;
        this.tradeAdvertUnitPrice = -1;
        this.botPublicChatMessage = "";
        this.botPublicChatColor = -1;
        this.botPublicChatEffect = -1;
        this.pendingTradeTarget = null;
        this.lastBotStallCheckX = 0;
        this.lastBotStallCheckY = 0;
        this.lastBotStallCheckPlane = 0;
        this.lastBotStallCheckExperience = 0L;
        this.savedWorldRouteIndex = -1;
        this.currentWorldRouteChoice = null;
        this.botTaskItemId = -1;
        this.botSmithingProductItemId = -1;
        this.botShopItemAmount = -1;
        this.botUseTaskItemOnTarget = false;
        this.botShopBuyMode = -1;
        this.botSkillTargetSkillId = -1;
        this.botSkillTargetLevel = -1;
        this.botReservedGoalByte1 = -1;
        this.botReservedGoalByte2 = -1;
        this.botReservedGoalByte3 = -1;
        this.botReservedGoalByte4 = -1;
        this.botCompletionItemId = -1;
        this.botCompletionItemAmount = -1;
        this.botSecondaryCompletionItemId = -1;
        this.botReservedGoalInt2 = -1;
        this.botReservedGoalInt3 = -1;
        this.botReservedGoalInt4 = -1;
        this.savedWorldRouteReversed = false;
        this.botElementalSpellIndex = -1;
        this.currentBotTaskTypeId = -1;
        this.currentBotTaskIndex = -1;
        this.deferredBotTaskTypeId = -1;
        this.deferredBotTaskIndex = -1;
        this.botCombatLoadoutItemIds = new ArrayList();
        this.botMeleeLoadoutItemIds = new ArrayList();
        this.botRangedLoadoutItemIds = new ArrayList();
        this.botMagicLoadoutItemIds = new ArrayList();
        this.botShopSellItemIds = new ArrayList();
        this.botCombatLoadoutSlotCursor = -1;
        this.botLumbridgeResetPending = false;
        this.botStallSampleCount = 0;
        this.botPvpTeamInviteTicks = 0;
        this.botPvpPendingTeamTarget = null;
        this.botPvpRejectedTeamTargets = new ArrayList();
        this.botPvpTeamRequesters = new ArrayList();
        this.botPvpChatSource = null;
        this.botPvpChatMessage = null;
        this.questStates = new int[QuestDefinition.questStateCapacity];
        this.questProgressFlags = new int[QuestDefinition.questStateCapacity];
        this.questHookStates = new int[100];
        this.visibleDynamicObjects = new ArrayList();
        this.pendingDynamicObjectRemovals = new ArrayList();
        this.agilityCourseProgress = 0;
        this.gnomeAgilityCourseProgress = 0;
        this.piratesTreasureBananaCrateCount = 0;
        this.hintedNpcIndex = -1;
        this.profileString1 = "";
        this.profileString2 = "";
        this.barrowsDoorPuzzleSolved = false;
        this.activeBarrowsDoorPuzzleIndex = -1;
        this.barrowsRewardPotential = 0;
        this.activeBarrowsDoorPuzzleAnswerObjectIds = new int[3];
        this.barrowsChestOpened = false;
        this.flourMillHopperGrainCount = 0;
        this.reservedSaveLong1 = 0L;
        this.reservedSaveInt3 = 0;
        this.reservedSaveInt2 = 0;
        this.membershipExpiresMillis = 0L;
        this.expiredMembershipRelocationRequired = false;
        this.ringOfWealthShinePending = false;
        this.playerOptionTextCache = new String[5];
        this.multiwayAreaState = false;
        this.musicManagerTrackId = -1;
        this.displayedWildernessLevel = -1;
        this.displayedBarrowsKillCount = -1;
        this.gatheringHazardCounter = 0;
        this.lastPacketReceivedMillis = 0L;
        this.lastPlayerInputMillis = 0L;
        this.familyCrestGauntletItemId = 778;
        this.logoutPacketSent = false;
        this.lastRegionChangeMillis = 0L;
        this.botLootResumeTarget = null;
        this.botLootGroundItems = new ArrayList();
        this.botLootPickupTargets = new ArrayList();
        this.botLootSellGroundItems = new ArrayList();
        this.botLootSellItems = new ArrayList();
        this.clanWarsBot = false;
        this.clanWarsTeamId = -1;
        this.botTaskSavedElapsedMillis = 0L;
        this.botEscapeStuckTicks = 0;
        this.botCombatEscapeActive = false;
        this.botMagicPenaltyGearUnequipped = false;
        this.botAntipoisonAvailable = false;
        this.botActiveCombatStyle = 0;
        this.botPrimaryCombatStyle = 0;
        this.botSpecialCombatStyle = 0;
        this.botOpponentCombatStyle = 0;
        this.botCombatStyle = 0;
        this.botSpecialAttackEnergyCost = 0;
        this.botStrengthPotionDepleted = false;
        this.botFoodDepleted = false;
        this.botWeaponItemId = 0;
        this.botShieldItemId = 0;
        this.botSpecialWeaponItemId = 0;
        this.botCombatSpell = null;
        this.botFoodItemId = 0;
        this.botWildernessMaxY = 0;
        this.botCombatState = null;
        this.botMagicGearSwapDelayTicks = 0;
        this.botThreatEscapeDelayTicks = 0;
        this.botPrayerSwitchDelayTicks = 0;
        this.botQueuedPrayerId = -1;
        this.botEatDelayTicks = 0;
        this.botWeaponSwapDelayTicks = 0;
        this.isBot = false;
        this.bonesToPeachesUnlocked = false;
        this.botPathSegmentIndex = -1;
        this.botTargetNpcId = -1;
        this.botPathWaypointIndex = -1;
        this.botEscapeRouteName = "";
        this.currentBotRoute = null;
        this.botRouteActionPending = false;
        this.botRouteTravelPending = false;
        this.botInteractionTargetIds = new ArrayList();
        this.botEnabled = false;
        this.botInteractionOption = 1;
        this.botTaskState = "";
        this.pendingCropResurrectionPatchIndex = -1;
        this.pendingCropResurrectionPatchType = "";
        this.currentBankTab = 0;
        this.gangAffiliation = 0;
        this.grandExchangeSellOfferFlags = new boolean[6];
        this.grandExchangeItemIds = new int[6];
        this.grandExchangeQuantities = new int[6];
        this.grandExchangeUnitPrices = new int[6];
        this.grandExchangeCancelledFlags = new boolean[6];
        this.grandExchangeCompletedQuantities = new int[6];
        this.grandExchangeTotalPrices = new int[6];
        this.grandExchangePrimaryCollectAmounts = new int[6];
        this.grandExchangeSecondaryCollectAmounts = new int[6];
        this.grandExchangeFinishMessagePending = new boolean[6];
        this.selectedGrandExchangeItemId = 0;
        this.selectedGrandExchangeQuantity = 0;
        this.selectedGrandExchangeUnitPrice = 0;
        this.selectedGrandExchangeSlot = 0;
        this.mageArenaFlamesOfZamorakCastsRemaining = 100;
        this.mageArenaClawsOfGuthixCastsRemaining = 100;
        this.mageArenaSaradominStrikeCastsRemaining = 100;
        this.mageArenaProgressStage = 0;
        new ArrayList();
        this.fightCaveWaveIndex = 0;
        this.godModeEnabled = false;
        this.fightCaveNpcs = new ArrayList();
        this.loadedCharacterFromBackup = false;
        this.legacyQuestPoints = 0;
        this.npcKillCount = 0;
        this.playerKillCount = 0;
        this.deathCount = 0;
        this.easyCluesCompleted = 0;
        this.mediumCluesCompleted = 0;
        this.hardCluesCompleted = 0;
        this.soldItemsValue = 0;
        this.boughtItemsValue = 0;
        this.duelWins = 0;
        this.duelLosses = 0;
        this.barrowsRunsCompleted = 0;
        this.skeletonSkinUnlocked = 0;
        this.loginRestrictionExempt = false;
        this.loginInitializationComplete = false;
        this.cluePuzzleSolved = false;
        this.automaticMusicEnabled = true;
        this.configStates = new int[2000];
        this.activeRecurringEffectId = 0;
        this.activeEnvironmentalHazardId = 0;
        this.hiscorePage = 0;
        this.hiscoreCategory = 21;
        this.missingNpcSearchRetryCount = 0;
        this.botStallRecoveryAttempts = 0;
        this.cutsceneActive = false;
        this.suppressTeleportCleanup = false;
        this.planeChangeRefreshPending = false;
        this.pendingPublicChatText = "";
        this.fightCaveSpawnRotation = 0;
        this.prayerDrainAccumulator = 0;
        this.prayerDrainRate = 0;
        this.grandExchangeSettlementInProgress = false;
        this.enterTheAbyssMiniquestState = 0;
        this.swampCaveRopeAttached = false;
        this.lampOilStillFilled = false;
        this.caveInsectSwarmStage = 0;
        this.swampGasFlareState = 0;
        this.selectionKey = selectionKey;
        this.inboundBuffer = ByteBuffer.allocateDirect(512);
        this.outboundBuffer = ByteBuffer.allocateDirect(8192);
        if (selectionKey != null) {
            this.socketChannel = (SocketChannel)((SelectionKey)selectionKey).channel();
            this.hostAddress = this.socketChannel.socket().getInetAddress().getHostAddress();
        }
        this.setPosition(new Position(ServerSettings.startX, ServerSettings.startY, ServerSettings.startPlane));
        this.getAttributes().put("smithing", Boolean.FALSE);
        this.getAttributes().put("smelting", Boolean.FALSE);
        this.getAttributes().put("isBanking", Boolean.FALSE);
        this.getAttributes().put("isShopping", Boolean.FALSE);
        this.getAttributes().put("canPickup", Boolean.FALSE);
        this.getAttributes().put("canTakeDamage", Boolean.TRUE);
        this.resetAppearance();
        this.appearanceColors[0] = 7;
        this.appearanceColors[1] = 0;
        this.appearanceColors[2] = 9;
        this.appearanceColors[3] = 5;
        this.appearanceColors[4] = 0;
        int index = 0;
        while (index < this.queuedLoginItemIds.length) {
            this.queuedLoginItemIds[index] = -1;
            this.queuedLoginItemAmounts[index] = 0;
            ++index;
        }
        this.pvpCombatReferences = new LinkedList();
    }

    public final void resetAppearance() {
        int index = 0;
        Object value = this;
        this.gender = index;
        int[] integerValues = ServerSettings.APPEARANCE_BODY_PART_RANGES[this.gender][0];
        value = integerValues;
        System.arraycopy(integerValues, 0, this.appearanceParts, 0, 7);
        int[] integerValues2 = ServerSettings.APPEARANCE_COLOR_RANGES[this.gender][0];
        value = integerValues2;
        System.arraycopy(integerValues2, 0, this.appearanceColors, 0, 5);
    }

    public final void applyDefaultMaleAppearance() {
        int index = 0;
        Object value = this;
        this.gender = index;
        value = this;
        ((Player)value).appearanceParts[0] = 86;
        value = this;
        ((Player)value).appearanceParts[1] = 87;
        value = this;
        ((Player)value).appearanceParts[2] = 89;
        value = this;
        ((Player)value).appearanceParts[3] = 84;
        value = this;
        ((Player)value).appearanceParts[4] = 88;
        value = this;
        ((Player)value).appearanceParts[5] = 90;
        value = this;
        ((Player)value).appearanceParts[6] = 85;
        int[] integerValues = ServerSettings.APPEARANCE_COLOR_RANGES[this.gender][0];
        value = integerValues;
        System.arraycopy(integerValues, 0, this.appearanceColors, 0, 5);
        this.setAppearanceUpdateRequired(true);
        this.getUpdateState().setUpdateRequired(true);
    }

    public final void dispatchCurrentPacket() {
        Object value = PacketBuffer.wrapReader(this.inboundBuffer);
        value = new IncomingPacket(this.currentPacketOpcode, this.currentPacketLength, (PacketReader)value);
        PacketDispatcher.packetTimers[((IncomingPacket)value).getOpcode()].start();
        Iterator iterator = this.playerPlugins.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        PacketDispatcher.dispatchPacket(this, (IncomingPacket)value);
        PacketDispatcher.packetTimers[((IncomingPacket)value).getOpcode()].stop();
    }

    public final void completeAllQuestStates() {
        int index = 0;
        while (index < QuestDefinition.questCount) {
            this.questStates[index] = 1;
            ++index;
        }
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     */
    public final void randomizeAppearance() {
        isMemberControlExit2: {
            int value;
            int[] integerValues;
            int[] integerValues2;
            Player player;
            randomizeAppearanceControlExit1: {
                getMembershipDaysRemainingControlExit1: {
                    getMembershipDaysRemainingControlExit3: {
                        int value2 = GameUtil.randomInt(2);
                        player = this;
                        this.gender = value2;
                        player = this;
                        integerValues2 = ServerSettings.APPEARANCE_COLOR_RANGES[player.gender][0];
                        player = this;
                        integerValues = ServerSettings.APPEARANCE_COLOR_RANGES[player.gender][1];
                        value = 0;
                        if (!true) break getMembershipDaysRemainingControlExit3;
                        player = this;
                        if (value >= player.appearanceColors.length) break getMembershipDaysRemainingControlExit1;
                    }
                    do {
                        player = this;
                        player.appearanceColors[value] = integerValues2[value] + GameUtil.randomInt(integerValues[value] - integerValues2[value]);
                        ++value;
                        player = this;
                    } while (value < player.appearanceColors.length);
                }
                player = this;
                integerValues2 = ServerSettings.APPEARANCE_BODY_PART_RANGES[player.gender][0];
                player = this;
                integerValues = ServerSettings.APPEARANCE_BODY_PART_RANGES[player.gender][1];
                value = 0;
                if (!true) break randomizeAppearanceControlExit1;
                player = this;
                if (value >= player.appearanceParts.length) break isMemberControlExit2;
            }
            do {
                player = this;
                if (player.gender == 1 && value == 6) {
                    player = this;
                    player.appearanceParts[value] = -1;
                } else {
                    player = this;
                    player.appearanceParts[value] = integerValues2[value] + GameUtil.randomInt(integerValues[value] - integerValues2[value]);
                }
                ++value;
                player = this;
            } while (value < player.appearanceParts.length);
        }
        this.setAppearanceUpdateRequired(true);
        this.getUpdateState().setUpdateRequired(true);
    }
    public final void writePacketBuffer(ByteBuffer byteBuffer) {
        if (!this.socketChannel.isOpen()) {
            return;
        }
        byteBuffer.flip();
        try {
            synchronized (this) {
                if (this.outboundBuffer.position() == 0) {
                    this.socketChannel.write(byteBuffer);
                }
                if (!byteBuffer.hasRemaining()) {
                    return;
                }
                this.ensureOutboundCapacity(byteBuffer.remaining());
                this.outboundBuffer.put(byteBuffer);
                DedicatedReactor reactor = DedicatedReactor.getInstance();
                synchronized (reactor) {
                    reactor.getSelector().wakeup();
                    this.selectionKey.interestOps(this.selectionKey.interestOps() | 4);
                }
            }
        } catch (Exception exception) {
            this.disconnect();
        }
    }

    private void ensureOutboundCapacity(int additionalBytes) {
        if (additionalBytes <= this.outboundBuffer.remaining()) {
            return;
        }
        int required = this.outboundBuffer.position() + additionalBytes;
        int capacity = this.outboundBuffer.capacity();
        while (capacity < required) {
            int doubled = capacity << 1;
            capacity = doubled > capacity ? doubled : required;
        }
        ByteBuffer replacement = ByteBuffer.allocateDirect(capacity);
        this.outboundBuffer.flip();
        replacement.put(this.outboundBuffer);
        this.outboundBuffer = replacement;
    }
    public final void disconnect() {
        disconnectControlExit1: {
            Object value;
            Object value2;
            if (this.loginInitializationComplete) {
                long value3 = System.currentTimeMillis() + 15000L;
                value2 = this;
                this.disconnectGraceExpiresAtMillis = value3;
                value = PlayerConnectionState.DISCONNECTING;
                value2 = this;
                this.connectionState = (PlayerConnectionState)value;
            }
            if (!this.isBot) {
                this.selectionKey.attach(null);
                this.selectionKey.cancel();
            }
            if (this.currentGroup != null) {
                this.currentGroup.removeMember(this);
            }
            try {
                try {
                    if (this != null && this.loginInitializationComplete) {
                        value2 = this;
                        if (((Player)value2).username != null) {
                            value2 = this;
                            if (((Player)value2).password != null) {
                                value2 = this;
                                ((Player)value2).fightCaveController.cleanupIfInFightCave();
                                value2 = this;
                                if (((Player)value2).loginResponseCode == 2) {
                                    CharacterFileManager.savePlayer(this);
                                }
                            }
                        }
                    }
                    if (!this.isBot) {
                        this.socketChannel.close();
                        ConnectionThrottle.releaseConnectionSlot(this.hostAddress);
                    }
                }
                catch (Exception exception) {
                    value2 = exception;
                    exception.printStackTrace();
                    if (this.getIndex() != -1) {
                        value = Server.getDisconnectQueue();
                        synchronized (value) {
                            Server.getDisconnectQueue().offer(this);
                            return;
                        }
                    }
                    break disconnectControlExit1;
                }
            }
            catch (Throwable throwable) {
                if (this.getIndex() != -1) {
                    value = Server.getDisconnectQueue();
                    synchronized (value) {
                        Server.getDisconnectQueue().offer(this);
                    }
                }
                throw throwable;
            }
            if (this.getIndex() != -1) {
                value = Server.getDisconnectQueue();
                synchronized (value) {
                    Server.getDisconnectQueue().offer(this);
                    return;
                }
            }
        }
    }

    public final void showHiscoreInterface(int interfaceId) {
        int index = 0;
        while (index < 16) {
            this.packetSender.sendInterfaceText("", 18819 + 4 * index);
            this.packetSender.sendInterfaceText("", 18820 + 4 * index);
            this.packetSender.sendInterfaceText("", 18821 + 4 * index);
            this.packetSender.sendInterfaceText("", 18822 + 4 * index);
            ++index;
        }
        ArrayList<CharacterFileRecord> arrayList = new ArrayList<CharacterFileRecord>();
        for (Object recordObject : CharacterFileManager.liveHiscoreRecords) {
            CharacterFileRecord characterFileRecord = (CharacterFileRecord)recordObject;
            if (characterFileRecord.playerRights >= 2 || characterFileRecord.gameMode != interfaceId) continue;
            arrayList.add(characterFileRecord);
        }
        if (interfaceId == 3) {
            for (Object recordObject : CharacterFileManager.deadHardcoreIronmanRecords) {
                arrayList.add((CharacterFileRecord)recordObject);
            }
        }
        int value = this.hiscoreCategory;
        int value2 = this.hiscorePage;
        String text = value != 22 ? String.valueOf(value < 21 ? SkillManager.SKILL_NAMES[value] : "Overall") + " Hiscores" : "Wealth Hiscores";
        String text2 = "";
        if (interfaceId == 1) {
            text2 = "<img=3>";
        } else if (interfaceId == 2) {
            text2 = "<img=4>";
        }
        if (interfaceId == 3) {
            text2 = String.valueOf(text2) + "<img=5>";
        }
        this.packetSender.sendInterfaceText(String.valueOf(text2) + text + text2, 18814);
        Collections.sort(arrayList, new HiscoreEntryComparator(this, value));
        int value3 = value2 << 4;
        while (value3 < arrayList.size()) {
            CharacterFileRecord characterFileRecord = (CharacterFileRecord)arrayList.get(value3);
            int value4 = value3 - (value2 << 4);
            if (value3 == ((value2 + 1) << 4)) break;
            if (!(value == 22 ? characterFileRecord.getStoredItemValue() < 100000 : value < 21 && CharacterFileRecord.getLevelForExperience(characterFileRecord.getSkillExperience(value)) < 30)) {
                String text3 = "@bla@";
                if (characterFileRecord.username.equals(this.username)) {
                    text3 = "@whi@";
                }
                boolean enabled = characterFileRecord.memberFlag;
                int value5 = characterFileRecord.playerRights;
                String text4 = "";
                if (value5 == 1) {
                    text4 = String.valueOf(text4) + "<img=0>";
                }
                if (value5 == 2) {
                    text4 = String.valueOf(text4) + "<img=1>";
                }
                if (enabled && value5 < 2) {
                    text4 = String.valueOf(text4) + "<img=2>";
                }
                if (interfaceId == 3 && characterFileRecord.gameMode != 3) {
                    text4 = "<img=6>" + text4;
                }
                this.packetSender.sendInterfaceText(String.valueOf(text3) + (value3 + 1), 18819 + 4 * value4);
                this.packetSender.sendInterfaceText(String.valueOf(text3) + text4 + characterFileRecord.username, 18820 + 4 * value4);
                String text5 = "";
                String text6 = "";
                if (value < 21) {
                    text5 = GameUtil.formatNumber((long)CharacterFileRecord.getLevelForExperience(characterFileRecord.getSkillExperience(value)));
                    text6 = GameUtil.formatNumber(characterFileRecord.getSkillExperience(value));
                }
                if (value == 21) {
                    text5 = GameUtil.formatNumber((long)characterFileRecord.getTotalLevel());
                    text6 = GameUtil.formatNumber(characterFileRecord.getSkillExperience(value));
                }
                if (value == 22) {
                    text5 = GameUtil.formatCompactAmountHighThreshold(characterFileRecord.getStoredItemValue());
                }
                this.packetSender.sendInterfaceText(String.valueOf(text3) + text5, 18821 + 4 * value4);
                this.packetSender.sendInterfaceText(String.valueOf(text3) + text6, 18822 + 4 * value4);
            }
            ++value3;
        }
        this.packetSender.showInterface(18788);
    }

    public final void completeQuestJournal() {
        Player player;
        int value = 18;
        if (!ServerSettings.freeToPlayWorld) {
            value = 104;
        }
        int initialValue = 1;
        while (initialValue <= value) {
            QuestDefinition questDefinition = QuestDefinition.forId(initialValue);
            player = this;
            player.packetSender.sendInterfaceTextColor(questDefinition.getJournalButtonId(), Color.GREEN);
            this.questStates[initialValue] = 1;
            this.gangAffiliation = GameUtil.randomInt(2) + 1;
            ++initialValue;
        }
        player = this;
        player.questManager.refreshQuestPointText();
    }

    private void resetQuestJournal() {
        Player player;
        int value = 18;
        if (!ServerSettings.freeToPlayWorld) {
            value = 104;
        }
        int initialValue = 1;
        while (initialValue <= value) {
            QuestDefinition questDefinition = QuestDefinition.forId(initialValue);
            player = this;
            player.packetSender.sendInterfaceTextColor(questDefinition.getJournalButtonId(), Color.RED);
            this.questStates[initialValue] = 0;
            this.questProgressFlags[initialValue] = 0;
            this.gangAffiliation = 0;
            ++initialValue;
        }
        player = this;
        player.questManager.refreshQuestPointText();
    }

    public final void executeCheatCommand(String text2, String[] stringValues2) {
        this.executeCheatCommand(text2, stringValues2, true);
    }

    public final void executeCheatCommand(String text4, String[] stringValues2, boolean enabled2) {
        if (text4.equals("runes")) {
            int value = 554;
            while (value <= 564) {
                this.inventoryManager.addItem(new ItemStack(value, 10000));
                ++value;
            }
            if (!ServerSettings.freeToPlayWorld) {
                this.inventoryManager.addItem(new ItemStack(565, 10000));
                this.inventoryManager.addItem(new ItemStack(566, 10000));
            }
        } else if (text4.equals("nfood")) {
            this.inventoryManager.addItem(new ItemStack(334, 1000));
            this.inventoryManager.addItem(new ItemStack(330, 1000));
            this.inventoryManager.addItem(new ItemStack(362, 1000));
            this.inventoryManager.addItem(new ItemStack(380, 1000));
            this.inventoryManager.addItem(new ItemStack(374, 1000));
            if (!ServerSettings.freeToPlayWorld) {
                this.inventoryManager.addItem(new ItemStack(386, 1000));
                this.inventoryManager.addItem(new ItemStack(392, 1000));
            }
        } else if (text4.equals("food")) {
            if (ServerSettings.freeToPlayWorld) {
                this.inventoryManager.addItem(new ItemStack(379, 20));
            } else {
                this.inventoryManager.addItem(new ItemStack(385, 20));
            }
        } else if (text4.equals("money")) {
            this.inventoryManager.addItem(new ItemStack(995, 1000000));
        } else if (text4.equals("arrows")) {
            this.inventoryManager.addItem(new ItemStack(884, 10000));
            this.inventoryManager.addItem(new ItemStack(886, 10000));
            this.inventoryManager.addItem(new ItemStack(888, 10000));
            this.inventoryManager.addItem(new ItemStack(890, 10000));
            if (!ServerSettings.freeToPlayWorld) {
                this.inventoryManager.addItem(new ItemStack(892, 10000));
            }
        } else if (text4.equals("empty")) {
            Player player = this;
            player.inventoryManager.getContainer().clear();
            player = this;
            player.inventoryManager.refresh();
        } else if (text4.equals("bank")) {
            BankManager.openBank(this);
        } else if (text4.equals("god")) {
            this.godModeEnabled = !this.godModeEnabled;
            Player player = this;
            player.packetSender.sendGameMessage("God mode: " + (this.godModeEnabled ? "enabled" : "disabled"));
        } else if (text4.equals("master")) {
            int index = 0;
            while (index < this.skillManager.getCurrentLevels().length - 1) {
                this.skillManager.getCurrentLevels()[index] = ServerSettings.maxLevel;
                Player player = this;
                this.skillManager.getExperience()[index] = SkillManager.getExperienceForLevel(ServerSettings.maxLevel - 1);
                ++index;
            }
            this.skillManager.refreshAllSkills();
        } else if (text4.equals("setlevel")) {
            int value2 = Integer.parseInt(stringValues2[0]);
            int value3 = Integer.parseInt(stringValues2[1]);
            value3 = value3 > ServerSettings.maxLevel ? ServerSettings.maxLevel : value3;
            Player player = this;
            player.skillManager.getCurrentLevels()[value2] = value3;
            Player player2 = this;
            player = player2;
            player = this;
            player2.skillManager.getExperience()[value2] = SkillManager.getExperienceForLevel(value3 - 1);
            player = this;
            player.skillManager.refreshSkill(value2);
        } else if (text4.equals("run")) {
            this.setRunEnergyPercent(100);
        } else if (text4.equals("irun")) {
            this.infiniteRunEnabled = !this.infiniteRunEnabled;
            Player player = this;
            player.packetSender.sendGameMessage("Infinite run set to: " + (this.infiniteRunEnabled ? "enabled" : "disabled"));
        } else if (text4.equals("music")) {
            MusicManager.unlockAllTracks(this);
        } else if (text4.equals("pray")) {
            Player player = this;
            Player player3 = player;
            player3 = this;
            player.skillManager.setCurrentLevel(5, player3.skillManager.getBaseLevel(5));
            player3 = this;
            player3.skillManager.refreshSkill(5);
        } else if (text4.equals("nbones")) {
            this.inventoryManager.addItem(new ItemStack(527, 1000));
            this.inventoryManager.addItem(new ItemStack(533, 1000));
            if (!ServerSettings.freeToPlayWorld) {
                this.inventoryManager.addItem(new ItemStack(537, 1000));
            }
        } else if (text4.equals("quests")) {
            this.completeQuestJournal();
        } else if (text4.equals("rquests")) {
            this.resetQuestJournal();
        } else if (text4.equals("sitem")) {
            int value4 = Integer.parseInt(stringValues2[0]);
            if ((value4 < 7956 || value4 > 8118) && value4 >= 0 && ItemDefinition.isDefined(value4)) {
                ItemDefinition itemDefinition;
                int initialValue = 1;
                if (((String[])stringValues2).length > 1) {
                    initialValue = Integer.parseInt(stringValues2[1]);
                    int value5 = initialValue = initialValue > Integer.MAX_VALUE ? Integer.MAX_VALUE : initialValue;
                }
                if (!(itemDefinition = ItemDefinition.forId(value4)).isStackable() && initialValue > 28) {
                    value4 = itemDefinition.getNotedId();
                }
                ItemStack itemStack = new ItemStack(value4, initialValue);
                this.inventoryManager.addItem(itemStack);
            }
        } else if (text4.equals("char")) {
            Player player = this;
            player.packetSender.showInterface(3559);
        } else if (text4.equals("rchar")) {
            this.randomizeAppearance();
        } else if (text4.equals("skiptask")) {
            Player player = this;
            player.slayerManager.completeTask();
        } else if (text4.equals("bot")) {
            if (!this.botEnabled || this.isBot) {
                Player player = this;
                Player player4 = this;
                player4.packetSender.sendGameMessage("Bot started.");
                this.moveTo(this.getPosition());
                StartBotCommandTask startBotCommandTask = new StartBotCommandTask(this, 2, player);
                World.getTaskScheduler().schedule(startBotCommandTask);
            }
        } else if (text4.equals("sbot")) {
            if (this.isBot) {
                BotPlayer botPlayer = (BotPlayer)this;
                if (this.botMode == 1) {
                    botPlayer.startCombatLoadoutBot();
                } else if (this.botMode == 0) {
                    botPlayer.startSkillingBot();
                } else if (this.botMode == 2) {
                    botPlayer.startTradeAdvertBot();
                } else if (this.botMode == 3) {
                    botPlayer.startDropPartyBot();
                } else if (this.botMode == 4) {
                    botPlayer.startProgressiveBot();
                } else if (this.botMode == 5 || this.botMode == 6) {
                    botPlayer.startClanWarsBot(this.botMode);
                } else if (this.botMode == 7) {
                    botPlayer.startMinigameBot();
                }
            }
        } else if (text4.equals("modern")) {
            Player player = this;
            player.packetSender.setSidebarInterface(6, 1151);
            this.spellbook = Spellbook.MODERN;
        } else if (text4.equals("ancient")) {
            Player player = this;
            player.packetSender.setSidebarInterface(6, 12855);
            this.spellbook = Spellbook.ANCIENT;
        } else if (text4.equals("tele")) {
            try {
                int value6 = Integer.parseInt(stringValues2[0]);
                int value7 = Integer.parseInt(stringValues2[1]);
                int value8 = Integer.parseInt(stringValues2[2]);
                this.moveTo(new Position(value6, value7, value8));
            }
            catch (Exception exception) {
                try {
                    int value9 = Integer.parseInt(stringValues2[0]);
                    int value10 = Integer.parseInt(stringValues2[1]);
                    this.moveTo(new Position(value9, value10, this.getPosition().getPlane()));
                }
                catch (Exception exception2) {}
            }
        } else if (text4.equals("cquest")) {
            try {
                int value11 = Integer.parseInt(stringValues2[0]);
                QuestDefinition questDefinition = QuestDefinition.forId(value11);
                Player player = this;
                player.packetSender.sendInterfaceTextColor(questDefinition.getJournalButtonId(), Color.GREEN);
                this.questStates[value11] = 1;
                if (value11 == 16) {
                    this.gangAffiliation = GameUtil.randomInt(2) + 1;
                }
                player = this;
                player.questManager.refreshQuestPointText();
            }
            catch (Exception exception) {}
        } else if (text4.equals("rquest")) {
            try {
                int value12 = Integer.parseInt(stringValues2[0]);
                QuestDefinition questDefinition = QuestDefinition.forId(value12);
                Player player = this;
                player.packetSender.sendInterfaceTextColor(questDefinition.getJournalButtonId(), Color.RED);
                this.questStates[value12] = 0;
                this.questProgressFlags[value12] = 0;
                if (value12 == 16) {
                    this.gangAffiliation = 0;
                }
                player = this;
                player.questManager.refreshQuestPointText();
            }
            catch (Exception exception) {}
        }
        Player player = this;
        player.packetSender.sendGameMessage("Cheat activated.");
        if (ServerSettings.broadcastCheatUsageEnabled && enabled2) {
            String text2 = text4;
            if (text2.equals("sitem")) {
                text2 = "item";
            }
            player = this;
            String text3 = String.valueOf(player.username) + " used cheat: " + text2;
            System.out.println(text3);
            Player[] playerArray = World.getPlayers();
            int length = playerArray.length;
            int index2 = 0;
            while (index2 < length) {
                player = playerArray[index2];
                if (player != null && player != this) {
                    player.packetSender.sendGameMessage(text3);
                }
                ++index2;
            }
        }
    }

    public final void handleCommand(String password, String[] stringValues2, String text22) {
        Object value;
        int value2;
        if ((password = password.toLowerCase()).equals("item")) {
            if (!this.isActionLocked()) {
                this.packetSender.closeInterfaces();
                this.packetSender.showInterface(19103);
            }
            return;
        } else if (password.equals("pos")) {
            this.packetSender.sendGameMessage(
                    this.getPosition().getX() + ", "
                    + this.getPosition().getY() + ", "
                    + this.getPosition().getPlane());
            return;
        } else if (password.equals("tick")) {
            if (stringValues2 == null || stringValues2.length == 0) {
                this.packetSender.sendGameMessage(
                        "Farming minute interval: " + Server.getMinuteIntervalTicks() + " game ticks.");
                return;
            }
            int intervalTicks;
            if (stringValues2[0].equalsIgnoreCase("reset")) {
                intervalTicks = Server.DEFAULT_MINUTE_INTERVAL_TICKS;
            } else {
                try {
                    intervalTicks = Integer.parseInt(stringValues2[0]);
                }
                catch (NumberFormatException exception) {
                    this.packetSender.sendGameMessage("Usage: ::tick [positive number|reset]");
                    return;
                }
            }
            if (intervalTicks < 1) {
                this.packetSender.sendGameMessage("Tick interval must be at least 1.");
                return;
            }
            Server.setMinuteIntervalTicks(intervalTicks);
            this.packetSender.sendGameMessage(
                    "Farming minute interval set to " + intervalTicks + " game ticks.");
            return;
        } else if (password.equals("debug")) {
            this.interactionDebugEnabled = !this.interactionDebugEnabled;
            System.out.println("[interaction-debug] player=" + this.username + " enabled="
                    + this.interactionDebugEnabled);
            this.packetSender.sendGameMessage(
                    "Debug is " + (this.interactionDebugEnabled ? "on" : "off") + ".");
            return;
        } else if (password.equals("pk")) {
            if (this.currentGroup == null) {
                this.packetSender.sendGameMessage("You have to be in group in order to use this command.");
                return;
            }
            if (this.currentGroup != null && this.currentGroup.leader == this) {
                this.packetSender.sendGameMessage("You can't be the group leader to use this command.");
                return;
            }
            this.botEnabled = !this.botEnabled;
            this.packetSender.sendGameMessage("Pkbot " + (this.botEnabled ? "enabled." : "disabled."));
            if (this.botEnabled) {
                BotCombatLoadoutManager.startGroupCombatBot(this);
            }
        } else if (password.equals("ms")) {
            this.movementSystemMode = value2 = this.movementSystemMode == 0 ? 1 : 0;
            this.packetSender.sendGameMessage("Movement system set to: " + value2);
        } else if (password.equals("lquest")) {
            try {
                String text3 = stringValues2[0].toLowerCase();
                int initialValue = 1;
                while (initialValue < QuestDefinition.questCount) {
                    value = QuestDefinition.forId(initialValue);
                    ((QuestDefinition)value).getName();
                    if (((QuestDefinition)value).getName().toLowerCase().contains(text3)) {
                        this.packetSender.sendGameMessage("[" + initialValue + "] " + ((QuestDefinition)value).getName());
                    }
                    ++initialValue;
                }
            }
            catch (Exception exception) {}
        } else if (password.equals("home")) {
            value = this;
            ((Player)value).packetSender.setSidebarInterface(6, 1151);
            this.spellbook = Spellbook.MODERN;
            MagicSpellAction.castSelfSpell(this, SpellDefinition.HOME_TELEPORT);
        } else if (password.equals("tp")) {
            WorldTeleportMenu.open(this);
            return;
        } else if (password.equals("classicui")) {
            // Sent by the client's "Classic interface" setting on login and whenever it changes.
            boolean enabled = stringValues2.length > 0 && stringValues2[0].equalsIgnoreCase("on");
            if (enabled != this.classicSidebar) {
                this.classicSidebar = enabled;
                com.rs2.net.packet.ClassicSidebar.refreshTabs(this);
            }
            return;
        } else if (password.equals("postcutoff")) {
            // Sent by the client's "post-2006 content" setting on login and whenever it changes.
            boolean enabled = stringValues2.length > 0 && stringValues2[0].equalsIgnoreCase("on");
            if (enabled != ServerSettings.content2007Enabled) {
                ServerSettings.content2007Enabled = enabled;
                if (enabled) {
                    GodWarsDungeonManager.spawnGodWarsNpcs();
                }
                this.packetSender.sendGameMessage("Post-2006 content (God Wars Dungeon, Grand Exchange) "
                        + (enabled ? "enabled." : "disabled."));
            }
            return;
        } else if (password.equals("runes")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("master")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("setlevel")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("modern")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("ancient") && ServerSettings.cacheVersion >= 308) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("nfood")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("food")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("skiptask")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("bot")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("money")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("arrows")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("empty")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("bank")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("god")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("run")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("irun")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("music")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("pray")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("nbones")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("quests")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("rquests")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("rquest")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("cquest")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("sitem")) {
            int value3 = Integer.parseInt(stringValues2[0]);
            if ((value3 < 7956 || value3 > 8118) && value3 >= 0 && ItemDefinition.isDefined(value3)) {
                this.executeCheatCommand(password, stringValues2);
            }
        } else if (password.equals("char")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("rchar")) {
            this.executeCheatCommand(password, stringValues2);
        } else if (password.equals("tele")) {
            try {
                this.executeCheatCommand(password, stringValues2);
            }
            catch (Exception exception) {
                try {
                    this.executeCheatCommand(password, stringValues2);
                }
                catch (Exception exception2) {}
            }
        }
        value = this;
        if (((Player)value).playerRights >= 2 && !password.equals("bug") && !password.equals("yell")) {
            new StringBuilder(String.valueOf(password)).append(stringValues2.length > 0 ? " " + text22 : "");
        }
        value = this;
        if (((Player)value).playerRights >= 0) {
            if (password.equals("inv")) {
                if (this.gameMode != 0) {
                    this.packetSender.sendGameMessage("You are not playing on normal gamemode and cannot form a group.");
                    return;
                }
                if (this.currentGroup != null && this.currentGroup.leader != this) {
                    this.packetSender.sendGameMessage("You can only invite as a group leader.");
                    return;
                }
                Player player = World.findPlayerByUsername(text22);
                if (player == null) {
                    return;
                }
                if (player.gameMode != 0) {
                    value = player;
                    this.packetSender.sendGameMessage(String.valueOf(((Player)value).username) + " is not playing on normal gamemode and cannot join a group.");
                    return;
                }
                if (player.currentGroup != null && player.pendingGroupInviteTarget == this) {
                    PlayerGroup.handleGroupInvite(this, player);
                    return;
                }
                if (player.currentGroup != null && this.currentGroup != null && player.currentGroup == this.currentGroup) {
                    value = player;
                    this.packetSender.sendGameMessage("You kicked " + ((Player)value).username + " from the group.");
                    this.currentGroup.removeMember(player);
                    return;
                }
                if (player.currentGroup != null) {
                    value = player;
                    this.packetSender.sendGameMessage(String.valueOf(((Player)value).username) + " is already in a group.");
                    return;
                }
                PlayerGroup.handleGroupInvite(this, player);
                return;
            }
            if (password.equals("pw")) {
                if (stringValues2.length != 2) {
                    this.packetSender.sendGameMessage("usage: ::pw oldPassword newPassword");
                    return;
                }
                String text4 = stringValues2[0];
                String text5 = stringValues2[1];
                value = this;
                if (((Player)value).password.equals(text4)) {
                    password = text5;
                    value = this;
                    this.password = password;
                    this.packetSender.sendGameMessage("Password set to: " + text5);
                    return;
                }
                this.packetSender.sendGameMessage("Incorrect old password!");
                return;
            }
            if (password.equals("statsme") && ServerSettings.cacheVersion >= 254) {
                value2 = GameplayHelper.getDaysBetweenMidnights(this.createdAtMillis, System.currentTimeMillis());
                if (value2 == 0) {
                    value2 = 1;
                }
                this.clearStatsInterfaceText();
                this.sendStatsInterfaceLines(new String[]{"@whi@Your stats", "", "@blu@Account created: " + GameplayHelper.formatDateDayMonthYear(this.createdAtMillis) + " (d-m-y)", "Npcs killed: " + this.npcKillCount, "Players killed: " + this.playerKillCount, "Deaths: " + this.deathCount, "@whi@--Dueling--", "Wins: " + this.duelWins, "Losses: " + this.duelLosses, "@whi@--Barrows--", "Runs done: " + this.barrowsRunsCompleted, "@whi@--Clues Completed--", "Easy: " + this.easyCluesCompleted, "Medium: " + this.mediumCluesCompleted, "Hard: " + this.hardCluesCompleted, "@whi@--Shopping--", "Worth of sold items: " + GameUtil.formatCompactAmountHighThreshold(this.soldItemsValue), "Worth of bought items: " + GameUtil.formatCompactAmountHighThreshold(this.boughtItemsValue), "Profit: " + GameUtil.formatCompactAmountHighThreshold(this.soldItemsValue - this.boughtItemsValue), "@whi@--Playing time--", "This session: " + GameplayHelper.formatDurationHoursMinutes(this.getSessionPlaytimeMillis()), "Total: " + GameplayHelper.formatDurationHoursMinutes(this.getTotalPlaytimeMillis()), "Average per day: " + GameplayHelper.formatDurationHoursMinutes(this.getTotalPlaytimeMillis() / (long)value2)});
                value = this;
                ((Player)value).packetSender.showInterface(8134);
                if (ServerSettings.clientBuild == 443) this.packetSender.sendInterfaceScrollPosition(8143, 0);
                return;
            }
            if (password.equals("players")) {
                this.packetSender.sendGameMessage("There are currently " + World.getPlayerCount() + " players online.");
            }
        }
    }

    private void startCurrentBotTaskInteraction() {
        this.botTaskState = "do task";
        this.botInteractionOption = this.currentBotTask.getInteractionOption(this);
        if (!this.dropPartyLeader && !this.currentBotTask.usesCustomTaskAction) {
            if (this.currentBotTask.interactionTargetType == 0) {
                this.interactWithBotObjectTargets(this.botInteractionTargetIds);
            } else {
                this.interactWithBotNpcTargets(this.botInteractionTargetIds);
            }
        }
        if (this.currentBotTask.usesCustomTaskAction) {
            this.currentBotTask.startCustomTaskAction(this);
        }
    }

    public final void continueBotRoute() {
        if (this.currentBotRoute == null) {
            return;
        }
        if (this.isMoving()) {
            return;
        }
        if (this.currentBotTask != null
                && this.currentBotTask.taskRouteSegments != null
                && ("walk towards task".equals(this.botTaskState)
                    || "walk to task".equals(this.botTaskState)
                    || "walk towards bank".equals(this.botTaskState)
                    || "walk to bank".equals(this.botTaskState))
                && (this.botPathSegmentIndex < 0
                    || this.botPathSegmentIndex >= this.currentBotTask.taskRouteSegments.length)) {
            Player player = this;
            System.out.println(String.valueOf(player.username)
                    + " has invalid bot task route segment index "
                    + this.botPathSegmentIndex + " for "
                    + this.currentBotTask.getClass().getSimpleName()
                    + " (" + this.currentBotTask.taskRouteSegments.length
                    + " segments), restarting route.");
            if (this.botTaskState.contains("bank")) {
                this.currentBotTask.startWalkToBank(this);
            } else {
                this.currentBotTask.startWalkToTask(this);
            }
            return;
        }
        this.getMovementQueue().clearMovementActions();
        if (this.botTaskState.equals("do task") && DropPartyBotManager.dropPartyParticipants.contains(this)) {
            this.currentBotRoute = null;
            this.botPathWaypointIndex = 0;
            return;
        }
        if (this.botPathWaypointIndex == -1) {
            Player player = this;
            System.out.println("Error! " + player.username + " does not know where to walk!");
            return;
        }
        Position position = this.currentBotRoute.waypoints[this.botPathWaypointIndex];
        int distance = GameUtil.getDistance(this.getPosition(), position);
        if (distance > 50) {
            Player player = this;
            System.out.println("Detected possibly frozen bot: " + player.username + " at: " + this.getPosition() + ", trying to apply fix.");
            this.moveTo(position);
            player = this;
            System.out.println(String.valueOf(player.username) + " teleported to current destination: " + position);
        }
        PathFinder.getInstance();
        boolean path = PathFinder.findPath(this, position.getX(), position.getY(), false, 0, 0);
        if (!path) {
            boolean enabled = false;
            if (this.botUseTaskItemOnTarget) {
                this.botUseTaskItemOnTarget = false;
                enabled = true;
            }
            Player player = this;
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            arrayList.add(player.botTargetNpcId);
            int value = 3;
            value = 3;
            value = 0;
            Player player2 = player;
            player2.interactWithBotObjectTargets(arrayList, false, 3, 3);
            if (enabled) {
                this.botUseTaskItemOnTarget = true;
            }
        }
        if (!path) {
            return;
        }
        if (this.getPosition().equals(position)) {
            ++this.botPathWaypointIndex;
            if (this.botPathWaypointIndex == this.currentBotRoute.waypoints.length) {
                this.currentBotRoute = null;
                this.botPathWaypointIndex = 0;
                if (this.botTaskState.equals("walk to task")) {
                    this.startCurrentBotTaskInteraction();
                    return;
                }
                if (this.botTaskState.equals("walk pretask path1")) {
                    if (this.dropPartyPretaskLoopCount >= DropPartyBotManager.dropPartyPretaskLoopLimit) {
                        this.dropPartyPretaskComplete = true;
                    }
                    if (this.dropPartyPretaskComplete) {
                        this.currentBotTask.startWalkToTask(this);
                        return;
                    }
                    this.currentBotTask.returnPretaskPath(this);
                    ++this.dropPartyPretaskLoopCount;
                    return;
                }
                if (this.botTaskState.equals("walk pretask path2")) {
                    this.currentBotTask.startPretaskPath(this);
                    return;
                }
                if (this.botTaskState.equals("walk to bank") || this.botTaskState.equals("worldwalk to bank")) {
                    this.botTaskState = "empty inventory";
                    if (this.currentBotTask.usesDepositBox) {
                        this.botInteractionOption = 2;
                        ArrayList<Integer> arrayList = new ArrayList<Integer>();
                        arrayList.add(2619);
                        this.interactWithBotNpcTargets(arrayList);
                        return;
                    }
                    this.botInteractionOption = 2;
                    ArrayList<Integer> arrayList = new ArrayList<Integer>();
                    arrayList.add(2213);
                    arrayList.add(11758);
                    this.interactWithBotObjectTargets(arrayList);
                    return;
                }
                if (this.botTaskState.startsWith("walk towards")) {
                    this.currentBotTask.advanceTaskRouteSegment(this, false);
                    return;
                }
                if (this.botTaskState.startsWith("world walk towards")) {
                    BotWorldRouteWalker.advanceRouteSegment(this);
                    return;
                }
                if (this.botTaskState.startsWith("world walk find")) {
                    BotWorldRouteWalker.findWorldRoute(this);
                    return;
                }
                if (this.botTaskState.startsWith("world walk finish")) {
                    this.currentWorldRouteChoice = null;
                    this.botTaskState = "empty inventory";
                    GameplayHelper.startBotTaskRoute(this);
                }
                return;
            }
            position = this.currentBotRoute.waypoints[this.botPathWaypointIndex];
        }
        if (position != null) {
            if (this.botMode == 4 && !this.getMovementQueue().isRunning() && this.getRunEnergyPercent() >= 80) {
                this.getMovementQueue().setRunning(true);
            }
            EntityTargetMovement.clearMovementTarget(this);
            PathFinder.getInstance();
            PathFinder.findGlobalPath(this, position.getX(), position.getY(), true, 0, 0);
            return;
        }
        Player player = this;
        player.packetSender.sendGameMessage("No destination!");
    }
    public final void interactWithBotNpcTargets(ArrayList arrayList) {
        Object value;
        Object value2;
        Position reachableInteractionPosition = null;
        interactWithBotNpcTargetsControlExit1: {
            interactWithBotNpcTargetsControlExit2: {
                Object value3;
                interactWithBotNpcTargetsControlExit3: {
                    int value4;
                    int npc = 0;
                    int value5;
                    int value6 = 30;
                    if (arrayList.size() == 1 && this.botMode == 4 && (value5 = ((Integer)arrayList.get(0)).intValue()) == -1 && !this.recoverBotTaskStall(true)) {
                        return;
                    }
                    if (this.currentBotTask != null && this.botTaskState.equals("do task")) {
                        if (this.currentBotTask.targetSearchRadius != -1) {
                            value6 = this.currentBotTask.targetSearchRadius;
                        }
                        if (this.currentBotTask.combatTask) {
                            if (this.botCombatStyle == 2 && this.botMode == 4 && this.botElementalSpellIndex != -1) {
                                this.setAutocastSpell(BotCombatLoadoutTables.elementalStrikeSpells[this.botElementalSpellIndex]);
                            }
                            if (this.botCombatStyle == 1) {
                                Player player = this;
                                value5 = player.equipmentManager.getItemIdAtSlot(3);
                                boolean enabled = false;
                                if (value5 > 0 && (((String)(value2 = ItemDefinition.forId(value5).getName().toLowerCase())).contains("bow") || ((String)value2).contains("knife") || ((String)value2).contains("dart") || ((String)value2).contains("javelin") || ((String)value2).contains("thrownaxe"))) {
                                    enabled = true;
                                }
                                if (!enabled) {
                                    this.botTaskReturnToBankRequested = true;
                                    this.currentBotTask.startWalkToBank(this);
                                    return;
                                }
                            }
                        }
                        if (this.currentBotTask.combatTask && this.currentBotTask.getForcedCombatStyle() != 2) {
                            Player player = this;
                            if (player.inventoryManager.getItemAmount(this.botFoodItemId) == 0 || this.inventoryManager.getContainer().getFreeSlots() == 0) {
                                this.currentBotTask.startWalkToBank(this);
                                return;
                            }
                        }
                    }
                    int value7 = 100;
                    Position position2 = this.getPosition();
                    value2 = null;
                    ArrayList<Object> arrayList2 = new ArrayList<Object>();
                    Object npcs = World.getNpcs();
                    int length = ((Npc[])npcs).length;
                    boolean enabled2 = false;
                    while (npc < length) {
                        value3 = ((Npc[])npcs)[npc];
                        if (value3 != null && !((Entity)value3).isDead() && ((Npc)value3).isActive() && ((Npc)value3).getOwnerPlayer() == null && (value4 = GameUtil.getDistance(position2, ((Entity)value3).getPosition())) <= value6) {
                            int position3 = ((Entity)value3).getPosition().getY();
                            int position4 = ((Entity)value3).getPosition().getX();
                            Player player = this;
                            int initialValue = -1;
                            int initialValue2 = -1;
                            int initialValue3 = -1;
                            int initialValue4 = -1;
                            if (player.currentBotTask != null && player.botTaskState.equals("do task")) {
                                if (player.currentBotTask.targetMinX != -1) {
                                    initialValue = player.currentBotTask.targetMinX;
                                }
                                if (player.currentBotTask.targetMinY != -1) {
                                    initialValue2 = player.currentBotTask.targetMinY;
                                }
                                if (player.currentBotTask.targetMaxX != -1) {
                                    initialValue3 = player.currentBotTask.targetMaxX;
                                }
                                if (player.currentBotTask.targetMaxY != -1) {
                                    initialValue4 = player.currentBotTask.targetMaxY;
                                }
                            }
                            if ((initialValue != -1 && position4 < initialValue ? false : (initialValue2 != -1 && position3 < initialValue2 ? false : (initialValue3 != -1 && position4 > initialValue3 ? false : initialValue4 == -1 || position3 <= initialValue4))) && (!((Npc)value3).getDefinition().isAttackable() || ((Entity)value3).isInMultiCombatArea() || ((Entity)value3).getSingleCombatTimer().hasElapsed())) {
                                Iterator iterator = arrayList.iterator();
                                while (iterator.hasNext()) {
                                    int integer = (Integer)iterator.next();
                                    if (integer != ((Npc)value3).getNpcId()) continue;
                                    arrayList2.add(value3);
                                    integer = GameUtil.getDistance(position2, ((Entity)value3).getPosition());
                                    if (integer >= value7) continue;
                                    value7 = integer;
                                }
                            }
                        }
                        ++npc;
                    }
                    value3 = new ArrayList();
                    for (Object positionObject : arrayList2) {
                        Npc position = (Npc)positionObject;
                        int distance = GameUtil.getDistance(position2, position.getPosition());
                        if (distance > value7 + 3) continue;
                        ((ArrayList)value3).add(position);
                    }
                    reachableInteractionPosition = null;
                    if (((ArrayList)value3).size() <= 1) break interactWithBotNpcTargetsControlExit3;
                    Collections.shuffle((ArrayList)value3);
                    npcs = ((ArrayList)value3).iterator();
                    while (((Iterator)npcs).hasNext()) {
                        Position position;
                        value = (Npc)((Iterator)npcs).next();
                        boolean position5 = GameUtil.hasClearPath(this.getPosition(), ((Entity)value).getPosition(), false);
                        value4 = position5 ? 1 : 0;
                        if (!position5) {
                            position = this.findReachableNpcInteractionPosition((Npc)value);
                            if (position == null) {
                                continue;
                            }
                            reachableInteractionPosition = position;
                        }
                        break interactWithBotNpcTargetsControlExit2;
                    }
                    break interactWithBotNpcTargetsControlExit1;
                }
                if (((ArrayList)value3).size() != 1) break interactWithBotNpcTargetsControlExit1;
                value = (Npc)((ArrayList)value3).get(0);
                boolean position6 = GameUtil.hasClearPath(this.getPosition(), ((Entity)value).getPosition(), false);
                if (!position6) {
                    reachableInteractionPosition = this.findReachableNpcInteractionPosition((Npc)value);
                }
            }
            value2 = value;
        }
        if (value2 != null) {
            int index = ((Entity)value2).getIndex();
            if (index < 0 || index > World.getNpcs().length) {
                return;
            }
            Npc npc = World.getNpcs()[index];
            if (npc == null || !npc.isInteractable()) {
                return;
            }
            Player player = this;
            player.packetSender.sendGameMessage("Nearby npc found: " + ((Npc)value2).getNpcId() + " [" + ((Entity)value2).getPosition() + "].");
            this.missingNpcSearchRetryCount = 0;
            this.botStallRecoveryAttempts = 0;
            if (reachableInteractionPosition != null) {
                PathFinder.getInstance();
                PathFinder.findPath(this, reachableInteractionPosition.getX(), reachableInteractionPosition.getY(), false, 0, 0);
            }
            if (npc.getDefinition().isAttackable()) {
                this.setAttackRange(1);
                this.setMovementTarget(npc);
                CombatManager.startCombat(this, npc);
                return;
            }
            int npcId = npc.getNpcId();
            player = this;
            this.interactionTargetId = npcId;
            npcId = npc.getPosition().getX();
            player = this;
            this.interactionTargetX = npcId;
            npcId = npc.getPosition().getY();
            player = this;
            this.interactionTargetY = npcId;
            npcId = this.getPosition().getPlane();
            player = this;
            this.interactionTargetPlane = npcId;
            npcId = index;
            player = this;
            this.interactionTargetIndex = npcId;
            this.getUpdateState().setFaceEntity(index);
            this.setAttackRange(1);
            this.setMovementTarget(npc);
            if (!this.botUseTaskItemOnTarget) {
                if (this.botInteractionOption == 1) {
                    InteractionDispatcher.setCurrentInteractionType(InteractionType.FIRST_NPC);
                }
                if (this.botInteractionOption == 2) {
                    InteractionDispatcher.setCurrentInteractionType(InteractionType.SECOND_NPC);
                }
                if (this.botInteractionOption == 3) {
                    InteractionDispatcher.setCurrentInteractionType(InteractionType.THIRD_NPC);
                }
            } else {
                player = this;
                npcId = player.inventoryManager.getContainer().indexOfItem(this.botTaskItemId);
                player = this;
                this.selectedItemSlot = npcId;
                npcId = this.botTaskItemId;
                player = this;
                this.selectedItemId = npcId;
                this.setInteractionTarget(npc);
                InteractionDispatcher.setCurrentInteractionType(InteractionType.ITEM_ON_NPC);
            }
            InteractionDispatcher.dispatchCurrentInteraction(this);
            return;
        }
        Player player = this;
        player.packetSender.sendGameMessage("Nearby npc not found.");
        ++this.missingNpcSearchRetryCount;
        if (this.missingNpcSearchRetryCount == 10) {
            this.missingNpcSearchRetryCount = 0;
            this.recoverBotTaskStall(true);
            return;
        }
        value = this;
        RetryMissingNpcSearchTask retryMissingNpcSearchTask = new RetryMissingNpcSearchTask(this, 10, (Player)value, arrayList);
        World.getTaskScheduler().schedule(retryMissingNpcSearchTask);
    }

    private Position findReachableNpcInteractionPosition(Npc npc) {
        Position position = null;
        int position4 = npc.getPosition().getX();
        int position5 = npc.getPosition().getY();
        int size = npc.getSize() - 1;
        int size2 = npc.getSize() - 1;
        int value = 40;
        int value2 = position4 - 1;
        while (value2 <= position4 + size + 1) {
            int value3 = position5 - 1;
            while (value3 <= position5 + size2 + 1) {
                if (value2 < position4 || value2 > position4 + size || value3 < position5 || value3 > position5 + size2) {
                    Position position2 = new Position(value2, value3);
                    Position position3 = GameUtil.findReachableInteractionPosition(npc.getPosition().getX(), npc.getPosition().getY(), position2.getX(), position2.getY(), npc.getSize(), npc.getSize(), this.getPosition().getPlane());
                    if (position3 != null) {
                        boolean position6 = GameUtil.hasClearPath(position2, npc.getPosition(), false);
                        PathFinder.getInstance();
                        boolean path = PathFinder.findPath(this, value2, value3, false, 0, 0);
                        int movementQueue = this.getMovementQueue().getSteps().size();
                        if (position6 && path && movementQueue < value) {
                            position = position2;
                            value = movementQueue;
                        }
                        this.getMovementQueue().reset();
                    }
                }
                ++value3;
            }
            ++value2;
        }
        return position;
    }

    public final boolean interactWithBotObjectTargetsNoRetry(ArrayList arrayList, boolean objectId) {
        return this.interactWithBotObjectTargets(arrayList, false, 20, 3);
    }

    public final boolean interactWithBotObjectTargets(ArrayList arrayList) {
        return this.interactWithBotObjectTargets(arrayList, true, 20, 3);
    }

    private WorldObject findBotTargetObjectAt(int objectId, int value2) {
        int initialValue = -1;
        int initialValue2 = -1;
        int initialValue3 = -1;
        int initialValue4 = -1;
        if (this.currentBotTask != null && this.botTaskState.equals("do task")) {
            if (this.currentBotTask.targetMinX != -1) {
                initialValue = this.currentBotTask.targetMinX;
            }
            if (this.currentBotTask.targetMinY != -1) {
                initialValue2 = this.currentBotTask.targetMinY;
            }
            if (this.currentBotTask.targetMaxX != -1) {
                initialValue3 = this.currentBotTask.targetMaxX;
            }
            if (this.currentBotTask.targetMaxY != -1) {
                initialValue4 = this.currentBotTask.targetMaxY;
            }
        }
        if (initialValue != -1 && objectId < initialValue) {
            return null;
        }
        if (initialValue2 != -1 && value2 < initialValue2) {
            return null;
        }
        if (initialValue3 != -1 && objectId > initialValue3) {
            return null;
        }
        if (initialValue4 != -1 && value2 > initialValue4) {
            return null;
        }
        WorldObject worldObject = SkillActionHelper.findWorldObjectAt(objectId, value2, this.getPosition().getPlane());
        if (worldObject != null) {
            ObjectManager.getInstance();
            DynamicObject dynamicObject = ObjectManager.findDynamicObjectAt(objectId, value2, this.getPosition().getPlane());
            if (dynamicObject != null) {
                worldObject = dynamicObject.getWorldObject();
            }
            return worldObject;
        }
        return null;
    }

    private boolean interactWithBotObjectTargets(ArrayList arrayList, boolean objectId, int objectId2, int value11) {
        WorldObject worldObject7;
        int value;
        int value2;
        if (arrayList.size() == 1 && this.botMode == 4 && (value2 = ((Integer)arrayList.get(0)).intValue()) == -1) {
            Player player = this;
            System.out.println("Detected bugged bot: " + player.username + ", trying to fix by reseting and relogging.");
            this.currentBotTask = null;
            this.deferredBotTask = null;
            this.applyTeleportPosition(new Position(ServerSettings.respawnX, ServerSettings.respawnY, ServerSettings.respawnPlane));
            World.logoutBotAndScheduleRelogin(this);
            return false;
        }
        if (this.currentBotTask != null) {
            if (this.botTaskState.equals("do task") && this.currentBotTask.targetSearchRadius != -1) {
                objectId2 = this.currentBotTask.targetSearchRadius;
            }
            if (this.botTaskState.equals("escape")) {
                return false;
            }
        }
        value2 = 100;
        WorldObject worldObject2 = null;
        ArrayList<WorldObject> arrayList2 = new ArrayList<WorldObject>();
        int position = this.getPosition().getX();
        int position2 = this.getPosition().getY();
        int index = 0;
        while (index <= objectId2) {
            int value3;
            if (index > value2 + value11) break;
            if (index == 0) {
                value = position;
                value3 = position2;
                worldObject7 = this.findBotTargetObjectAt(value, value3);
                if (worldObject7 != null) {
                    Iterator iterator = arrayList.iterator();
                    while (iterator.hasNext()) {
                        int integer = (Integer)iterator.next();
                        if (integer != worldObject7.getObjectId()) continue;
                        arrayList2.add(worldObject7);
                        int value4 = index;
                        if (value4 < value2) {
                            value2 = value4;
                        }
                        break;
                    }
                }
            } else {
                value = position2 - index;
                while (value <= position2 + index) {
                    value3 = position - index;
                    int value5 = value;
                    WorldObject worldObject3 = this.findBotTargetObjectAt(value3, value5);
                    if (worldObject3 != null) {
                        Iterator iterator = arrayList.iterator();
                        while (iterator.hasNext()) {
                            value3 = (Integer)iterator.next();
                            if (value3 != worldObject3.getObjectId()) continue;
                            arrayList2.add(worldObject3);
                            value3 = index;
                            if (value3 >= value2) break;
                            value2 = value3;
                            break;
                        }
                    }
                    ++value;
                }
                value = position2 - index;
                while (value <= position2 + index) {
                    value3 = position + index;
                    int value6 = value;
                    WorldObject worldObject4 = this.findBotTargetObjectAt(value3, value6);
                    if (worldObject4 != null) {
                        Iterator iterator = arrayList.iterator();
                        while (iterator.hasNext()) {
                            value3 = (Integer)iterator.next();
                            if (value3 != worldObject4.getObjectId()) continue;
                            arrayList2.add(worldObject4);
                            value3 = index;
                            if (value3 >= value2) break;
                            value2 = value3;
                            break;
                        }
                    }
                    ++value;
                }
                value = position - (index - 1);
                while (value <= position + (index - 1)) {
                    value3 = value;
                    int value7 = position2 - index;
                    WorldObject worldObject5 = this.findBotTargetObjectAt(value3, value7);
                    if (worldObject5 != null) {
                        Iterator iterator = arrayList.iterator();
                        while (iterator.hasNext()) {
                            value3 = (Integer)iterator.next();
                            if (value3 != worldObject5.getObjectId()) continue;
                            arrayList2.add(worldObject5);
                            value3 = index;
                            if (value3 >= value2) break;
                            value2 = value3;
                            break;
                        }
                    }
                    ++value;
                }
                value = position - (index - 1);
                while (value <= position + (index - 1)) {
                    value3 = value;
                    int value8 = position2 + index;
                    WorldObject worldObject6 = this.findBotTargetObjectAt(value3, value8);
                    if (worldObject6 != null) {
                        Iterator iterator = arrayList.iterator();
                        while (iterator.hasNext()) {
                            value3 = (Integer)iterator.next();
                            if (value3 != worldObject6.getObjectId()) continue;
                            arrayList2.add(worldObject6);
                            value3 = index;
                            if (value3 >= value2) break;
                            value2 = value3;
                            break;
                        }
                    }
                    ++value;
                }
            }
            ++index;
        }
        value = 0;
        boolean routeAvailable = false;

        if (arrayList2.size() > 1) {
            Collections.shuffle(arrayList2);
            for (WorldObject nearbyObject : arrayList2) {
                ObjectDefinition objectDefinition =
                        ObjectDefinition.forId(nearbyObject.getObjectId());
                if (objectDefinition == null) {
                    continue;
                }

                boolean alreadyReachable =
                        InteractionDispatcher.canReachObjectInteraction(
                                this, nearbyObject);
                boolean pathReachable = alreadyReachable
                        || PathFinder.isObjectReachable(
                                this,
                                nearbyObject.getPosition().getX(),
                                nearbyObject.getPosition().getY(),
                                Math.max(1, objectDefinition.width),
                                Math.max(1, objectDefinition.length),
                                nearbyObject.getType(),
                                nearbyObject.getOrientation(),
                                0);

                if (!pathReachable) {
                    continue;
                }

                worldObject2 = nearbyObject;
                value = GameUtil.getDistance(
                        this.getPosition(), nearbyObject.getPosition());
                routeAvailable = true;
                break;
            }
        } else if (arrayList2.size() == 1) {
            worldObject7 = (WorldObject)arrayList2.get(0);
            ObjectDefinition objectDefinition =
                    ObjectDefinition.forId(worldObject7.getObjectId());

            if (objectDefinition != null) {
                boolean alreadyReachable =
                        InteractionDispatcher.canReachObjectInteraction(
                                this, worldObject7);
                routeAvailable = alreadyReachable
                        || PathFinder.isObjectReachable(
                                this,
                                worldObject7.getPosition().getX(),
                                worldObject7.getPosition().getY(),
                                Math.max(1, objectDefinition.width),
                                Math.max(1, objectDefinition.length),
                                worldObject7.getType(),
                                worldObject7.getOrientation(),
                                0);
            }

            worldObject2 = worldObject7;
            value = GameUtil.getDistance(
                    this.getPosition(), worldObject7.getPosition());
        }

        if (worldObject2 != null) {
            Player player = this;
            player.packetSender.sendGameMessage(
                    "Nearby object found: " + worldObject2.getObjectId()
                    + " [" + worldObject2.getPosition() + "].");

            if (routeAvailable) {
                int position3 = worldObject2.getPosition().getX();
                this.interactionTargetX = position3;
                this.interactionTargetId = worldObject2.getObjectId();
                this.interactionTargetY = worldObject2.getPosition().getY();
                this.interactionTargetPlane =
                        worldObject2.getPosition().getPlane();

                EntityTargetMovement.clearMovementTarget(this);
                ObjectManager.prepareObjectInteractionMovement(
                        this,
                        this.interactionTargetId,
                        this.interactionTargetX,
                        this.interactionTargetY);

                boolean alreadyReachable =
                        InteractionDispatcher.canReachObjectInteraction(
                                this, worldObject2);
                if (alreadyReachable) {
                    this.getMovementQueue().clear();
                }
                boolean pathQueued = alreadyReachable
                        || ObjectInteractionPacketHandler
                                .queueObjectInteractionMovement(
                                        this, worldObject2);

                if (!pathQueued) {
                    player.packetSender.sendGameMessage(
                            "No position to walk to was found!");
                    if (objectId) {
                        TickTask retryTask =
                                new RetryUnreachableObjectTask(
                                        this, 10, this, arrayList);
                        World.getTaskScheduler().schedule(retryTask);
                    }
                    return false;
                }

                if (!this.botUseTaskItemOnTarget) {
                    if (this.botInteractionOption == 1) {
                        InteractionDispatcher.setCurrentInteractionType(
                                InteractionType.FIRST_OBJECT);
                    }
                    if (this.botInteractionOption == 2) {
                        InteractionDispatcher.setCurrentInteractionType(
                                InteractionType.SECOND_OBJECT);
                    }
                    if (this.botInteractionOption == 3) {
                        InteractionDispatcher.setCurrentInteractionType(
                                InteractionType.THIRD_OBJECT);
                    }
                } else {
                    position3 =
                            this.inventoryManager.getContainer()
                                    .indexOfItem(this.botTaskItemId);
                    this.selectedItemSlot = position3;
                    this.selectedItemId = this.botTaskItemId;
                    InteractionDispatcher.setCurrentInteractionType(
                            InteractionType.ITEM_ON_OBJECT);
                }

                InteractionDispatcher.dispatchCurrentInteraction(this);
                return true;
            }

            player.packetSender.sendGameMessage(
                    "No position to walk to was found!");
            if (objectId) {
                TickTask retryTask =
                        new RetryUnreachableObjectTask(
                                this, 10, this, arrayList);
                World.getTaskScheduler().schedule(retryTask);
            }
            return false;
        }

        Player player = this;
        player.packetSender.sendGameMessage("Nearby object not found.");
        if (objectId) {
            TickTask retryTask =
                    new RetryMissingObjectSearchTask(
                            this, 10, this, arrayList);
            World.getTaskScheduler().schedule(retryTask);
        }
        return false;
    }

    public static boolean isNpcAtRelativeOffset(Player player, Npc npc, int npcId, int value2) {
        int position = player.getPosition().getX();
        while (position < player.getPosition().getX() + player.getSize()) {
            int position2 = player.getPosition().getY();
            while (position2 < player.getPosition().getY() + player.getSize()) {
                int position3 = npc.getPosition().getX();
                while (position3 < npc.getPosition().getX() + npc.getSize()) {
                    int position4 = npc.getPosition().getY();
                    while (position4 < npc.getPosition().getY() + npc.getSize()) {
                        if (position3 == position + npcId && position4 == position2 + value2) {
                            return true;
                        }
                        ++position4;
                    }
                    ++position3;
                }
                ++position2;
            }
            ++position;
        }
        return false;
    }

    public final void scheduleDelayedMove(Position position) {
        Player player = this;
        player.packetSender.showInterface(8677);
        boolean enabled = true;
        player = this;
        this.actionLocked = enabled;
        CycleEventHandler.getInstance().schedule(this, new DelayedPositionMoveTask(this, position), 4);
    }

    public final void startBarrowsChestDamage() {
        Object value = this;
        if (((Player)value).currentWalkableInterfaceId == 4535) {
            int initialValue = -1;
            value = this;
            this.currentWalkableInterfaceId = initialValue;
            value = this;
            ((Player)value).packetSender.showWalkableInterface(-1);
        }
        value = this;
        ((Player)value).packetSender.sendCameraShake(2, 3, 2, 3);
        World.getTaskScheduler().schedule(new BarrowsChestDamageTask(this, 50));
        this.barrowsChestOpened = true;
    }

    public final void moveToInstancedPosition(Position position, boolean enabled3) {
        position.getPlane();
        boolean enabled2 = false;
        int plane = position.getPlane() + 4 + (this.getIndex() << 2);
        this.moveTo(new Position(position.getX(), position.getY(), plane));
    }

    public final void moveToPreservingInteractionState(Position position) {
        this.suppressTeleportCleanup = true;
        this.moveTo(position);
        this.suppressTeleportCleanup = false;
    }

    @Override
    public final void moveTo(Position position) {
        Player player;
        Player player2 = this;
        int position2 = player2.getPosition().getPlane();
        this.planeChangeRefreshPending = ((Position)position).getPlane() != position2;
        position2 = this.isActionLocked() ? 1 : 0;
        if (this.planeChangeRefreshPending) {
            GroundItemManager.getInstance();
            GroundItemManager.clearVisibleItems(player2);
        }
        if (position2 == 0) {
            boolean enabled = true;
            player = this;
            this.actionLocked = enabled;
        }
        if (!this.suppressTeleportCleanup) {
            this.resetInteractionState();
        }
        this.applyTeleportPosition((Position)position);
        if (!this.cutsceneActive && !this.isInBarrows()) {
            player = this;
            player.packetSender.sendMinimapState(0);
            if (!this.suppressTeleportCleanup) {
                player = this;
                player.packetSender.closeInterfaces();
            }
        }
        if (this.barrowsChestOpened && !this.isInBarrows()) {
            player = this;
            player.packetSender.resetCamera();
            this.barrowsChestOpened = false;
            BarrowsManager.resetBarrowsState(this);
        }
        CycleEventHandler.getInstance().schedule(this, new PostTeleportBotContinuationTask(this, position2 != 0, player2), 1);
    }

    public final void refreshRegionState() {
        GameplayHelper.refreshPlayerAreaOverlay(this);
        GameplayHelper.refreshRubberChickenPlayerOption(this);
        ObjectManager.getInstance().refreshDynamicObjectsForPlayer(this);
        GroundItemManager.getInstance();
        GroundItemManager.clearVisibleItems(this);
        GroundItemManager.getInstance().refreshForPlayer(this);
        Npc.refreshNearbyTransformedNpcs(this);
    }

    public final void applyTeleportPosition(Position position) {
        this.getPosition().set((Position)position);
        this.getPosition().setPreviousX(((Position)position).getX());
        this.getPosition().setPreviousY(((Position)position).getY() + 1);
        this.getMovementQueue().clear();
        this.teleportPlacementUpdateRequired = true;
        this.teleporting = true;
        this.packetSender.sendPlayerIndex();
    }

    public final void moveToGroundPosition(int value4, int value22, int value32) {
        this.moveTo(new Position(value4, value22, 0));
    }

    public final void sendLoginResponse() {
        if (this.isBot) {
            return;
        }
        if (this.isBanned()) {
            PacketWriter packetWriter = PacketBuffer.allocateWriter(3);
            Player player = this;
            packetWriter.writeByte(player.loginResponseCode);
            player = this;
            int hoursBetween = GameplayHelper.getHoursBetween(System.currentTimeMillis(), player.banExpires) + 1;
            packetWriter.writeShort(hoursBetween);
            this.writePacketBuffer(packetWriter.getBuffer());
            return;
        }
        PacketWriter packetWriter = PacketBuffer.allocateWriter(6);
        Player player = this;
        if (ServerSettings.clientBuild == 443) {
            System.out.println("[443-LOGIN] response username=" + player.getUsername()
                    + " code=" + player.loginResponseCode + " index=" + player.getIndex()
                    + " state=" + player.getConnectionState());
        }
        packetWriter.writeByte(player.loginResponseCode);
        if (player.loginResponseCode == 2) {
            packetWriter.writeByte(player.playerRights);
            packetWriter.writeByte(0);
            if (ServerSettings.clientBuild == 443) {
                packetWriter.writeShort(player.getIndex());
                packetWriter.writeByte(player.isMember() ? 1 : 0);
                this.writePacketBuffer(packetWriter.getBuffer());
                return;
            }
            packetWriter.writeByte(0);
            int loginGameMode = player.gameMode;
            if (loginGameMode < 0 || loginGameMode > 3) {
                System.err.println("Invalid login game mode " + loginGameMode + " for " + player.getUsername() + "; resetting to normal mode (0).");
                loginGameMode = 0;
                player.gameMode = 0;
            }
            packetWriter.writeByte(loginGameMode);
        }
        this.writePacketBuffer(packetWriter.getBuffer());
    }

    public final String getProfileString1() {
        return this.profileString1;
    }

    public final void setProfileString1(String profileString1) {
        this.profileString1 = profileString1;
    }

    public final void refreshEnterTheAbyssConfig() {
        int index = 0;
        if (this.enterTheAbyssMiniquestState == 1) {
            index = 4;
        } else if (this.enterTheAbyssMiniquestState >= 2) {
            index = 1;
        }
        this.configStates[492] = index;
        Player player = this;
        player.packetSender.sendConfig(492, this.configStates[492]);
    }

    public final void processPostLogin() {
        Player player;
        boolean enabled = this.validateLocalLogin();
        if (enabled && ServerSettings.clientBuild == 443) {
            World.registerPlayer(this);
        }
        this.sendLoginResponse();
        if (!enabled) {
            this.disconnect();
            return;
        }
        this.lastPacketReceivedMillis = System.currentTimeMillis();
        this.lastPlayerInputMillis = this.lastPacketReceivedMillis;
        if (ServerSettings.clientBuild == 443) {
            this.actionLocked = false;
            this.teleporting = true;
            this.setAppearanceUpdateRequired(true);
            this.getUpdateState().setUpdateRequired(true);
            this.registered = true;
            this.packetSender.sendMapRegion();
            InitialVarps.send(this);
        }
        if (this.expiredMembershipRelocationRequired) {
            Player player2 = this;
            this.membershipExpiresMillis = 0L;
            player2.applyTeleportPosition(TeleportManager.RESPAWN_TELEPORT_POSITION);
        }
        boolean relocatedFromCastleWars = CastleWarsManager.relocatePlayerOnLogin(this);
        Player player3 = this;
        int index = 0;
        if (ServerSettings.clientBuild != 443) {
            while (index < player3.configStates.length) {
                if (player3.configStates[index] != 0) {
                    player = player3;
                    player.packetSender.sendConfig(index, player3.configStates[index]);
                }
                ++index;
            }
            ErnestTheChickenQuest.refreshBasementLeverDoorConfig(player3);
            player3.refreshEnterTheAbyssConfig();
        }
        player = this;
        this.actionLocked = true;
        if (ServerSettings.clientBuild != 443) {
            World.registerPlayer(this);
        }
        if (relocatedFromCastleWars) {
            this.packetSender.sendGameMessage("You logged out during Castle Wars and have been returned to the lobby.");
        }
        this.packetSender.sendPostLoginState().syncPlayerConfigs();
        this.getPoisonDamage();
        this.getMovementQueue().isRunning();
        if (this.isInTenthSquadSigilInstance()) {
            int instancePlane = this.getPosition().getPlane() + 4 + (this.getIndex() << 2);
            this.moveTo(new Position(this.getPosition().getX(), this.getPosition().getY(), instancePlane));
            this.spawnTenthSquadSigilNpcs(instancePlane);
        }
        player = this;
        this.teleporting = true;
        PluginManager.attachPlayerPlugins(this);
        this.packetSender.sendPlayerOption("Follow", 2, false);
        this.packetSender.sendPlayerOption("Trade with", 3, false);
        this.skillManager.refreshAllSkills();
        this.registered = true;
        ItemStack weaponItem = this.equipmentManager.getContainer().getItemAt(3);
        if (weaponItem != null && weaponItem.getDefinition().isMembersOnly() && ServerSettings.freeToPlayWorld) {
            weaponItem = null;
        }
        if (weaponItem != null && weaponItem.getId() == 4024) {
            this.npcTransformationId = 1463;
        }
        this.getUpdateState().setUpdateRequired(true);
        this.setAppearanceUpdateRequired(true);
        this.setWeaponProfile(WeaponProfile.forItem(weaponItem));
        SpecialAttackDefinition specialAttackDefinition = SpecialAttackDefinition.forItem(weaponItem);
        player = this;
        this.specialAttackDefinition = specialAttackDefinition;
        player = this;
        player.duelSession.moveToDuelArenaExit();
        player = this;
        player.fightCaveController.cleanupIfInFightCave();
        player = this;
        player.packetSender.clearSidebarInterfaces();
        player = this;
        int combatLevel = player.skillManager.getCombatLevel();
        player = this;
        this.combatLevel = combatLevel;
        player = this;
        GameplayHelper.refreshRunecraftingTiaraConfig(this, player.equipmentManager.getItemIdAtSlot(0));
        player = this;
        player.equipmentManager.refreshWeaponAmmunitionState();
        player = this;
        player.equipmentManager.refreshBarrowsSetEffects();
        Player player4 = this;
        player4.inventoryManager.refresh();
        player4.equipmentManager.refresh();
        player4.bankPinManager.processPendingPinChanges();
        player = player4;
        World.scheduleTickTask(new PostLoginSyncTask(player4, 3, player));
        player4.prayerManager.deactivateAll();
        player4.skillManager.refreshAllSkills();
        player4.questManager.refreshQuestPointText();
        boolean enabled2 = false;
        player = this;
        this.actionLocked = enabled2;
        this.wildernessEntryAcknowledged = this.isInWilderness();
        Object value = this;
        MusicManager.unlockTrack((Player)value, 16);
        MusicManager.unlockTrack((Player)value, 321);
        MusicManager.unlockTrack((Player)value, 400);
        MusicManager.unlockTrack((Player)value, 466);
        MusicManager.unlockTrack((Player)value, 547);
        if (this.questStates[0] != 1) {
            player = this;
            player.questManager.refreshQuestJournal();
            if (this.questStates[0] >= 45) {
                player = this;
                player.equipmentManager.refreshWeaponInterface();
            }
        } else {
            player = this;
            player.packetSender.refreshSidebarInterfaces();
            player = this;
            player.equipmentManager.refreshWeaponInterface();
        }
        if (this.getCurrentHitpoints() <= 0) {
            CombatManager.handleDeath(this);
        }
        CacheDefinitionIndex.scheduleRandomEventRoll(this);
        this.setAppearanceUpdateRequired(true);
        if (ServerSettings.clientBuild != 443) {
            player = this;
            player.packetSender.sendInterfaceText("Total Lvl: " + this.skillManager.getTotalLevel(), 3984);
        }
        if (this.getPoisonDamage() > 0.0) {
            value = new HitDefinition(null, HitType.POISON, Math.ceil(this.getPoisonDamage())).setDelay(30);
            value = new CombatAction(this, this, (HitDefinition)value);
            PoisonEffect poisonEffect = new PoisonEffect(this.getPoisonDamage(), false);
            poisonEffect.apply((CombatAction)value);
        }
        int index2 = 0;
        while (index2 < 6) {
            if (this.grandExchangeFinishMessagePending[index2]) {
                GrandExchangeManager.sendOfferCompletionMessage(this, index2);
            }
            this.grandExchangeFinishMessagePending[index2] = false;
            ++index2;
        }
        if (this.barrowsChestOpened) {
            this.startBarrowsChestDamage();
        }
        player = this;
        if (player.telekineticTheatreController.isInsideTheatre()) {
            player = this;
            player.telekineticTheatreController.refreshCurrentMaze();
        }
        if (this.isBot && this.botMode == 4 && this.totalPlaytimeMillis > 0L) {
            this.executeCheatCommand("bot", null, false);
        }
        if (ServerSettings.clientBuild == 443 && !this.isBot) {
            // The 443 client stops its locally started title theme when login succeeds.
            // Send the area's normal music immediately so players do not have to move
            // once before the correct looping track begins (e.g. Newbie Melody on Tutorial Island).
            new MusicManager().updateForPlayerPosition(this);
        }
    }

    public final boolean loadAndValidateLogin() {
        CharacterFileManager.loadPlayer(this);
        if (this.validateLocalLogin()) {
            return true;
        }
        this.sendLoginResponse();
        LoginProtocol.releaseLoginUsername(this);
        this.disconnect();
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    private static boolean isUnsupportedUsernameCharacter(String text2) {
        int index = 0;
        while (index < 41) {
            if (allowedUsernameCharacters[index].equals(text2)) {
                return false;
            }
            ++index;
        }
        return true;
    }

    private static boolean hasValidUsernameCharacters(String text3) {
        text3 = text3.toLowerCase();
        int index = 0;
        while (index < text3.length()) {
            String text = "" + text3.charAt(index);
            if (Player.isUnsupportedUsernameCharacter(text)) {
                return false;
            }
            ++index;
        }
        return true;
    }

    public void initializeNewPlayer() {
    }

    private static boolean isReservedBotUsername(String text2) {
        if (text2.toLowerCase().startsWith("bot ")) {
            return true;
        }
        if (BotPlayer.botNamePool.contains(text2.toLowerCase())) {
            return true;
        }
        return BotPlayer.defaultProgressiveBotNames.contains(text2.toLowerCase());
    }

    private boolean validateLocalLogin() {
        boolean enabled = false;
        if (FileUtil.exists("./data/characters/" + this.username + ".dat")) {
            enabled = true;
        } else {
            if (this.username.toLowerCase().startsWith("mod ") || this.username.toLowerCase().startsWith("admin ") || this.username.toLowerCase().startsWith("owner ") || this.username.contains("  ") || !Player.hasValidUsernameCharacters(this.username) || Player.isReservedBotUsername(this.username) && !this.isBot) {
                int value = 3;
                Player player = this;
                this.loginResponseCode = value;
                return false;
            }
            this.createdAtMillis = System.currentTimeMillis();
            this.questRandomSeed = 1234 + GameUtil.randomInt(4321);
            if (this.isBot) {
                this.initializeNewPlayer();
            }
        }
        if (this.username.length() <= 0 || this.username.length() > 12 || this.submittedPassword.length() < 4 || this.submittedPassword.length() > 20 || !this.submittedPassword.equals(this.password) && enabled) {
            int value2 = 3;
            Player player = this;
            this.loginResponseCode = value2;
            return false;
        }
        Object value3 = this;
        if (!((Player)value3).hostAddress.equals("127.0.0.1")) {
            value3 = this;
            if (!((Player)value3).hostAddress.startsWith("192.168.")) {
                value3 = this;
                if (!((Player)value3).hostAddress.startsWith("10.")) {
                    int value4 = 11;
                    value3 = this;
                    this.loginResponseCode = value4;
                    return false;
                }
            }
        }
        if (World.getNonBotPlayerCount() >= 5) {
            int value5 = 11;
            value3 = this;
            this.loginResponseCode = value5;
            return false;
        }
        Object value6 = this.submittedPassword;
        value3 = this;
        this.password = (String)value6;
        value3 = this;
        if (((Player)value3).playerRights > 1 || this.username.toLowerCase().startsWith("bot ")) {
            value3 = this;
            if (!((Player)value3).hostAddress.equals("127.0.0.1")) {
                int value7 = 3;
                value3 = this;
                this.loginResponseCode = value7;
                return false;
            }
        }
        if (Player.isReservedBotUsername(this.username) && !this.isBot) {
            int value8 = 3;
            value3 = this;
            this.loginResponseCode = value8;
            return false;
        }
        if (Server.serverStatus == 3) {
            int value9 = 14;
            value3 = this;
            this.loginResponseCode = value9;
            return false;
        }
        if (World.getPlayerCount() >= ServerSettings.maxPlayers) {
            int value10 = 7;
            value3 = this;
            this.loginResponseCode = value10;
            return false;
        }
        Player[] playerArray = World.getPlayers();
        int length = playerArray.length;
        int index = 0;
        while (index < length) {
            value6 = playerArray[index];
            if (value6 != null) {
                Object value11 = value6;
                value3 = value11;
                value3 = this;
                if (((Player)value11).nameHash == ((Player)value3).nameHash) {
                    int value12 = 5;
                    value3 = this;
                    this.loginResponseCode = value12;
                    return false;
                }
                value3 = this;
                if (!((Player)value3).hostAddress.equals("127.0.0.1")) {
                    value3 = this;
                    if (((Player)value6).hostAddress.equals(((Player)value3).hostAddress)) {
                        int value13 = 9;
                        value3 = this;
                        this.loginResponseCode = value13;
                        return false;
                    }
                }
            }
            ++index;
        }
        value6 = this;
        if (((Player)value6).clientBuild != ServerSettings.clientBuild) {
            int value14 = 6;
            value3 = this;
            this.loginResponseCode = value14;
            return false;
        }
        value6 = this;
        if (((Player)value6).loginMagicByte != -1) {
            int value15 = 10;
            value3 = this;
            this.loginResponseCode = value15;
            return false;
        }
        int index2 = 0;
        String[] stringValues = blockedHostAddresses;
        int length2 = blockedHostAddresses.length;
        length = 0;
        while (length < length2) {
            String text = stringValues[length];
            value3 = this;
            if (((Player)value3).hostAddress.equals(text)) {
                index2 = 1;
                break;
            }
            ++length;
        }
        if (this.isBanned() || index2 != 0) {
            index2 = 4;
            value3 = this;
            this.loginResponseCode = index2;
            return false;
        }
        if (!this.isMember() && !ServerSettings.freeToPlayWorld) {
            index2 = 12;
            value3 = this;
            this.loginResponseCode = index2;
            return false;
        }
        value3 = this;
        if (!((Player)value3).memberFlag && !this.loginRestrictionExempt && ServerSettings.loginRestrictionMode == 1) {
            value3 = this;
            if (((Player)value3).playerRights == 0) {
                if (enabled) {
                    index2 = 12;
                    value3 = this;
                    this.loginResponseCode = index2;
                } else {
                    index2 = 29;
                    value3 = this;
                    this.loginResponseCode = index2;
                    CharacterFileManager.savePlayer(this);
                }
                return false;
            }
        }
        index2 = 2;
        value3 = this;
        this.loginResponseCode = index2;
        return true;
    }

    @Override
    public final void heal(int value2) {
        Player player = this;
        Player player2 = player;
        player2 = this;
        if (player.skillManager.getCurrentLevels()[3] + value2 <= player2.skillManager.getBaseLevel(3)) {
            player2 = this;
            int[] currentLevels = player2.skillManager.getCurrentLevels();
            currentLevels[3] = currentLevels[3] + value2;
        } else {
            Player player3 = this;
            player2 = player3;
            player2 = this;
            player3.skillManager.getCurrentLevels()[3] = player2.skillManager.getBaseLevel(3);
        }
        player2 = this;
        player2.skillManager.refreshSkill(3);
    }

    public String toString() {
        Player player = this;
        if (player.username == null) {
            player = this;
            return "Client(" + player.hostAddress + ")";
        }
        Player player2 = this;
        player = player2;
        Player player3 = this;
        player = player3;
        player = this;
        return "Player(" + player2.username + ":" + player3.password + " - " + player.hostAddress + ")";
    }

    public final void setBotHostAddress(String botHostAddress) {
        if (this.isBot) {
            this.hostAddress = botHostAddress;
            return;
        }
        System.err.println("setHost method can only be used for bot accounts!");
    }

    public final String getHostAddress() {
        return this.hostAddress;
    }

    public final Position getLastKnownRegionPosition() {
        return this.lastKnownRegionPosition;
    }

    public final void setTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }

    public final boolean isTeleporting() {
        return this.teleporting;
    }

    public final InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public final void setAppearanceUpdateRequired(boolean appearanceUpdateRequired) {
        if (appearanceUpdateRequired) {
            this.getUpdateState().setUpdateRequired(true);
        }
        this.appearanceUpdateRequired = appearanceUpdateRequired;
    }

    public final boolean isAppearanceUpdateRequired() {
        return this.appearanceUpdateRequired;
    }

    public final void setPlayerRights(int playerRights) {
        this.playerRights = playerRights;
    }

    public final int getPlayerRights() {
        return this.playerRights;
    }

    public final void setTeleportPlacementUpdateRequired(boolean teleportPlacementUpdateRequired) {
        this.teleportPlacementUpdateRequired = teleportPlacementUpdateRequired;
    }

    public final boolean isTeleportPlacementUpdateRequired() {
        return this.teleportPlacementUpdateRequired;
    }

    public final void setPublicChatColor(int color) {
        this.publicChatColor = color;
    }

    public final int getPublicChatColor() {
        return this.publicChatColor;
    }

    public final void setPublicChatEffects(int publicChatEffects) {
        this.publicChatEffects = publicChatEffects;
    }

    public final int getPublicChatEffects() {
        return this.publicChatEffects;
    }

    public final void setPublicChatAnimation(int animation) {
        this.publicChatAnimation = animation;
    }

    public final int getPublicChatAnimation() {
        return this.publicChatAnimation;
    }

    public final void setPublicChatPayload(byte[] publicChatPayload) {
        this.publicChatPayload = publicChatPayload;
    }

    public final byte[] getPublicChatPayload() {
        return this.publicChatPayload;
    }

    public final void flagAppearanceUpdate(boolean enabled2) {
        this.getUpdateState().setUpdateRequired(true);
        this.getUpdateState().setAppearanceUpdateRequired(true);
    }

    public final int[] getAppearanceParts() {
        return this.appearanceParts;
    }

    public final int[] getAppearanceColors() {
        return this.appearanceColors;
    }

    public final void setGender(int gender) {
        this.gender = gender;
    }

    public final int getGender() {
        return this.gender;
    }

    public final List getLocalPlayers() {
        return this.localPlayers;
    }

    public final void setLoginResponseCode(int loginResponseCode) {
        this.loginResponseCode = loginResponseCode;
    }

    public final List getLocalNpcs() {
        return this.localNpcs;
    }

    public final void setInteractionTargetX(int interactionTargetX) {
        this.interactionTargetX = interactionTargetX;
    }

    public final int getInteractionTargetX() {
        return this.interactionTargetX;
    }

    public final void setInteractionTargetY(int interactionTargetY) {
        this.interactionTargetY = interactionTargetY;
    }

    public final int getInteractionTargetY() {
        return this.interactionTargetY;
    }

    public final int getInteractionSpellButtonId() {
        return this.interactionSpellButtonId;
    }

    public final void setInteractionSpellButtonId(int buttonId) {
        this.interactionSpellButtonId = buttonId;
    }

    public final void setInteractionTargetId(int interactionTargetId) {
        this.interactionTargetId = interactionTargetId;
    }

    public final int getInteractionTargetId() {
        return this.interactionTargetId;
    }

    public final boolean isInteractionDebugEnabled() {
        return this.interactionDebugEnabled;
    }

    public final void setSelectedItemId(int itemId) {
        this.selectedItemId = itemId;
    }

    public final int getSelectedItemId() {
        return this.selectedItemId;
    }

    public final void setInteractionTargetIndex(int index) {
        this.interactionTargetIndex = index;
    }

    public final int getInteractionTargetIndex() {
        return this.interactionTargetIndex;
    }

    public final void setBankWithdrawNoteMode(boolean bankWithdrawNoteMode) {
        this.bankWithdrawNoteMode = bankWithdrawNoteMode;
    }

    public final boolean isBankWithdrawNoteMode() {
        return this.bankWithdrawNoteMode;
    }

    public final void setSelectedInterfaceItemId(int interfaceId) {
        this.selectedInterfaceItemId = interfaceId;
    }

    public final int getSelectedInterfaceItemId() {
        return this.selectedInterfaceItemId;
    }

    public final void setSelectedInterfaceSlot(int interfaceId) {
        this.selectedInterfaceSlot = interfaceId;
    }

    public final int getSelectedInterfaceSlot() {
        return this.selectedInterfaceSlot;
    }

    public final void setSelectedInterfaceId(int interfaceId) {
        this.selectedInterfaceId = interfaceId;
    }

    public final int getSelectedInterfaceId() {
        return this.selectedInterfaceId;
    }

    public final void setBankRearrangeMode(BankRearrangeMode bankRearrangeMode) {
        this.bankRearrangeMode = bankRearrangeMode;
    }

    public final BankRearrangeMode getBankRearrangeMode() {
        return this.bankRearrangeMode;
    }

    public final void setCurrentShopId(int currentShopId) {
        this.currentShopId = currentShopId;
    }

    public final int getCurrentShopId() {
        return this.currentShopId;
    }

    public final ItemContainer getBankContainer() {
        return this.bankContainer;
    }

    public final void setCombatBonus(int value3, int value22) {
        this.combatBonuses.put(value3, value22);
    }

    public final Map getCombatBonuses() {
        return this.combatBonuses;
    }

    public final long[] getFriendsList() {
        return this.friendsList;
    }

    public final void setRegistered(boolean registered) {
        this.registered = false;
    }

    public final boolean isRegistered() {
        return this.registered;
    }

    public final long[] getIgnoreList() {
        return this.ignoreList;
    }

    public final EquipmentManager getEquipmentManager() {
        return this.equipmentManager;
    }

    public final SkillManager getSkillManager() {
        return this.skillManager;
    }

    public final QuestManager getQuestManager() {
        return this.questManager;
    }

    public final RunecraftingObjectHandler getRunecraftingObjectHandler() {
        return this.runecraftingObjectHandler;
    }

    public final String getInterfaceAction() {
        return this.interfaceAction;
    }

    public final PacketSender getPacketSender() {
        return this.packetSender;
    }

    public final SlayerManager getSlayerManager() {
        return this.slayerManager;
    }

    public final AlchemistPlaygroundController getAlchemistPlaygroundController() {
        return this.alchemistPlaygroundController;
    }

    public final CreatureGraveyardController getCreatureGraveyardController() {
        return this.creatureGraveyardController;
    }

    public final TelekineticTheatreController getTelekineticTheatreController() {
        return this.telekineticTheatreController;
    }

    public final EnchantmentChamberController getEnchantmentChamberController() {
        return this.enchantmentChamberController;
    }

    public final DuelController getDuelController() {
        return this.duelController;
    }

    public final DuelSession getDuelSession() {
        return this.duelSession;
    }

    public final FightCaveController getFightCaveController() {
        return this.fightCaveController;
    }

    public final DuelInterfaceManager getDuelInterfaceManager() {
        return this.duelInterfaceManager;
    }

    public final DuelArenaLocationManager getDuelArenaLocationManager() {
        return this.duelArenaLocationManager;
    }

    public final Npc getOwnedNpc() {
        return this.ownedNpc;
    }

    public final WineFermentationHandler getWineFermentationHandler() {
        return this.wineFermentationHandler;
    }

    public final CookingManager getCookingManager() {
        return this.cookingManager;
    }

    public final ItemCombinationHandler getItemCombinationHandler() {
        return this.itemCombinationHandler;
    }

    public final SkillGuideManager getSkillGuideManager() {
        return this.skillGuideManager;
    }

    public final FoodHandler getFoodHandler() {
        return this.foodHandler;
    }

    public final PotionHandler getPotionHandler() {
        return this.potionHandler;
    }

    public final BankPinManager getBankPinManager() {
        return this.bankPinManager;
    }

    public final DialogueManager getDialogueManager() {
        return this.dialogueManager;
    }

    public final FiremakingHandler getFiremakingHandler() {
        return this.firemakingHandler;
    }

    public final MiningManager getMiningManager() {
        return this.miningManager;
    }

    public final CompostBinManager getCompostBinManager() {
        return this.compostBinManager;
    }

    public final boolean isFarmingLoginCatchUp() {
        return this.farmingLoginCatchUp;
    }

    public final void finishFarmingLoginCatchUp() {
        this.farmingLoginCatchUp = false;
    }

    public final AllotmentPatchManager getAllotmentPatchManager() {
        return this.allotmentPatchManager;
    }

    public final FlowerPatchManager getFlowerPatchManager() {
        return this.flowerPatchManager;
    }

    public final HerbPatchManager getHerbPatchManager() {
        return this.herbPatchManager;
    }

    public final HopsPatchManager getHopsPatchManager() {
        return this.hopsPatchManager;
    }

    public final BushPatchManager getBushPatchManager() {
        return this.bushPatchManager;
    }

    public final PlantPotHandler getPlantPotHandler() {
        return this.plantPotHandler;
    }

    public final TreePatchManager getTreePatchManager() {
        return this.treePatchManager;
    }

    public final FruitTreePatchManager getFruitTreePatchManager() {
        return this.fruitTreePatchManager;
    }

    public final SpecialTreePatchManager getSpecialTreePatchManager() {
        return this.specialTreePatchManager;
    }

    public final SpecialCropPatchManager getSpecialCropPatchManager() {
        return this.specialCropPatchManager;
    }

    public final FarmingToolStore getFarmingToolStore() {
        return this.farmingToolStore;
    }

    public final BoneBuryingHandler getBoneBuryingHandler() {
        return this.boneBuryingHandler;
    }

    public final FishingHandler getFishingHandler() {
        return this.fishingHandler;
    }

    public final SandwichLadyManager getSandwichLadyManager() {
        return this.sandwichLadyManager;
    }

    public final PetManager getPetManager() {
        return this.petManager;
    }

    public final SocialManager getSocialManager() {
        return this.socialManager;
    }

    public final void setTradeState(TradeState tradeState) {
        this.tradeState = tradeState;
    }

    public final TradeState getTradeState() {
        return this.tradeState;
    }

    public final ItemContainer getTradeOfferContainer() {
        return this.tradeOfferContainer;
    }

    public final ItemContainer getPartyRoomContainer() {
        return this.partyRoomContainer;
    }

    public final int[] getQueuedLoginItemIds() {
        return this.queuedLoginItemIds;
    }

    public final int[] getQueuedLoginItemAmounts() {
        return this.queuedLoginItemAmounts;
    }

    public final void setRunEnergyRaw(int runEnergyRaw) {
        if (runEnergyRaw < 0) {
            runEnergyRaw = 0;
        }
        if (runEnergyRaw > 10000) {
            runEnergyRaw = 10000;
        }
        this.runEnergyRaw = runEnergyRaw;
    }

    public final int getRunEnergyRaw() {
        return this.runEnergyRaw;
    }

    public final int getRunEnergyPercent() {
        double value = this.runEnergyRaw;
        double value2 = value / 10000.0 * 100.0;
        int value3 = (int)value2;
        return value3;
    }

    public final void addRunEnergyRaw(int value2) {
        Player player = this;
        this.setRunEnergyRaw(player.runEnergyRaw + value2);
    }

    public final void addRunEnergyPercent(int value4) {
        double value2 = value4;
        double value3 = (value2 /= 100.0) * 10000.0;
        value4 = (int)value3;
        this.addRunEnergyRaw(value4);
    }

    public final void setRunEnergyPercent(int runEnergyPercent) {
        double value = runEnergyPercent;
        double value2 = (value /= 100.0) * 10000.0;
        runEnergyPercent = (int)value2;
        this.setRunEnergyRaw(runEnergyPercent);
    }

    public final int getPrayerDrainThreshold() {
        int combatBonus = this.getCombatBonus(11);
        return 2 * combatBonus + 60;
    }

    public final ElapsedTimer getPacketReadTimer() {
        return this.packetReadTimer;
    }

    public final ByteBuffer getInboundBuffer() {
        return this.inboundBuffer;
    }

    public final SelectionKey getSelectionKey() {
        return this.selectionKey;
    }

    public final SocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    public final void setOutboundCipher(IsaacCipher isaacCipher) {
        this.outboundCipher = isaacCipher;
    }

    public final IsaacCipher getOutboundCipher() {
        return this.outboundCipher;
    }

    public final void setInboundCipher(IsaacCipher isaacCipher) {
        this.inboundCipher = isaacCipher;
    }

    public final IsaacCipher getInboundCipher() {
        return this.inboundCipher;
    }

    public final void setConnectionState(PlayerConnectionState playerConnectionState) {
        this.connectionState = playerConnectionState;
    }

    public final PlayerConnectionState getConnectionState() {
        return this.connectionState;
    }

    public final LoginProtocol getLoginProtocol() {
        return this.loginProtocol;
    }

    public final void setCurrentPacketOpcode(int opcode) {
        this.currentPacketOpcode = opcode;
    }

    public final int getCurrentPacketOpcode() {
        return this.currentPacketOpcode;
    }

    public final int getCurrentPacketLength() {
        return this.currentPacketLength;
    }

    public final void setCurrentPacketLength(int packetId) {
        this.currentPacketLength = packetId;
    }

    public final void setUsername(String username) {
        this.username = username;
    }

    public final String getUsername() {
        return this.username;
    }

    public final void setSubmittedPassword(String submittedPassword) {
        this.submittedPassword = submittedPassword;
    }

    public final void setPassword(String password) {
        this.password = password;
    }

    public final String getPassword() {
        return this.password;
    }

    public final void setPrayerHeadIcon(int prayerId) {
        this.prayerHeadIcon = prayerId;
    }

    public final int getPrayerHeadIcon() {
        return this.prayerHeadIcon;
    }

    public final String getProfileString2() {
        return this.profileString2;
    }

    public final void setProfileString2(String profileString2) {
        this.profileString2 = profileString2;
    }

    public final void subtractDonatorPoints(int value2) {
        this.donatorPoints -= value2;
    }

    public final void setDonatorPoints(int donatorPoints) {
        this.donatorPoints = donatorPoints;
    }

    public final int getDonatorPoints() {
        return this.donatorPoints;
    }

    public final int getSkullIcon() {
        return this.skullIcon;
    }

    public final boolean[] getActivePrayers() {
        return this.activePrayers;
    }

    public final PrayerManager getPrayerManager() {
        return this.prayerManager;
    }

    public final TeleportManager getTeleportManager() {
        return this.teleportManager;
    }

    public final EmoteManager getEmoteManager() {
        return this.emoteManager;
    }

    public final boolean isAutoRetaliate() {
        return this.autoRetaliate;
    }

    public final void setAutoRetaliate(boolean autoRetaliate) {
        this.autoRetaliate = autoRetaliate;
        // Character-file loading happens before the login response is sent.
        // Do not queue an encrypted in-game varp ahead of response code 2.
        // Post-login config synchronization sends varp 172 once the session is established.
        if (this.loginInitializationComplete) {
            this.packetSender.sendConfig(172, autoRetaliate ? 0 : 1);
        }
    }

    public final int getBrightness() {
        return this.brightness;
    }

    public final void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public final int getMouseButtons() {
        return this.mouseButtons;
    }

    public final void setMouseButtons(int buttonId) {
        this.mouseButtons = buttonId;
    }

    public final int getSplitPrivateChat() {
        return this.splitPrivateChat;
    }

    public final void setSplitPrivateChat(int splitPrivateChat) {
        this.splitPrivateChat = splitPrivateChat;
    }

    public final boolean isAcceptAidEnabled() {
        return this.acceptAid == 0;
    }

    public final int getAcceptAid() {
        return this.acceptAid;
    }

    public final void setAcceptAid(int acceptAid) {
        this.acceptAid = acceptAid;
    }

    public final int getMusicVolume() {
        return this.musicVolume;
    }

    public final void setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
    }

    public final int getEffectVolume() {
        return this.effectVolume;
    }

    public final void setEffectVolume(int effectVolume) {
        this.effectVolume = effectVolume;
    }

    public final int getQuestPoints() {
        int index = 0;
        int initialValue = 1;
        while (initialValue < QuestDefinition.questCount) {
            QuestScript questScript = QuestDefinition.getQuestScript(initialValue);
            if (this.questStates[initialValue] == 1) {
                index += questScript.getQuestPointReward();
            }
            ++initialValue;
        }
        return index;
    }

    public final void setSpellbook(Spellbook spellbook) {
        if (ServerSettings.cacheVersion < 308) {
            spellbook = Spellbook.MODERN;
        }
        this.spellbook = spellbook;
    }

    public final Spellbook getSpellbook() {
        return this.spellbook;
    }

    public final void addPlayerPlugin(PlayerPlugin playerPlugin) {
        this.playerPlugins.add(playerPlugin);
    }

    public final void setSpecialEnergy(int specialEnergy) {
        if (this.godModeEnabled) {
            this.specialEnergy = 100;
            return;
        }
        this.specialEnergy = specialEnergy;
        if (this.specialEnergy > 100) {
            this.specialEnergy = 100;
        }
    }

    public final int getSpecialEnergy() {
        return this.specialEnergy;
    }

    public final boolean isSpecialAttackEnabled() {
        return this.specialAttackEnabled;
    }

    public final void setSpecialAttackEnabled(boolean specialAttackEnabled) {
        this.specialAttackEnabled = specialAttackEnabled;
    }

    public final void setRingOfRecoilLife(int ringOfRecoilLife) {
        this.ringOfRecoilLife = ringOfRecoilLife;
    }

    public final int getRingOfRecoilLife() {
        return this.ringOfRecoilLife;
    }

    public final void setRingOfForgingLife(int ringOfForgingLife) {
        this.ringOfForgingLife = ringOfForgingLife;
    }

    public final int getRingOfForgingLife() {
        return this.ringOfForgingLife;
    }

    public final int getBindingNecklaceCharge() {
        return this.bindingNecklaceCharge;
    }

    public final void setBindingNecklaceCharge(int bindingNecklaceCharge) {
        this.bindingNecklaceCharge = bindingNecklaceCharge;
    }

    public final void setFightMode(int fightMode) {
        this.fightMode = fightMode;
    }

    public final int getFightMode() {
        return this.fightMode;
    }

    public final void setCrystalBowEquipped(boolean crystalBowEquipped) {
        this.crystalBowEquipped = crystalBowEquipped;
    }

    public final boolean isCrystalBowEquipped() {
        return this.crystalBowEquipped;
    }

    public final void setActionLocked(boolean actionLocked) {
        this.actionLocked = actionLocked;
    }

    public final boolean isActionLocked() {
        if (this.npcTransformationId == 2626 || this.npcTransformationId >= 3689 && this.npcTransformationId <= 3694) {
            return true;
        }
        return this.actionLocked;
    }

    public final void setAmmunitionDropsEnabled(boolean ammunitionDropsEnabled) {
        this.ammunitionDropsEnabled = ammunitionDropsEnabled;
    }

    public final boolean isAmmunitionDropsEnabled() {
        return this.ammunitionDropsEnabled;
    }

    public final void setDharokSetEffectActive(boolean dharokSetEffectActive) {
        this.dharokSetEffectActive = dharokSetEffectActive;
    }

    public final boolean isDharokSetEffectActive() {
        return this.dharokSetEffectActive;
    }

    public final void setAhrimSetEffectActive(boolean ahrimSetEffectActive) {
        this.ahrimSetEffectActive = ahrimSetEffectActive;
    }

    public final boolean isAhrimSetEffectActive() {
        return this.ahrimSetEffectActive;
    }

    public final void setKarilSetEffectActive(boolean karilSetEffectActive) {
        this.karilSetEffectActive = karilSetEffectActive;
    }

    public final boolean isKarilSetEffectActive() {
        return this.karilSetEffectActive;
    }

    public final void setToragSetEffectActive(boolean toragSetEffectActive) {
        this.toragSetEffectActive = toragSetEffectActive;
    }

    public final boolean isToragSetEffectActive() {
        return this.toragSetEffectActive;
    }

    public final void setGuthanSetEffectActive(boolean guthanSetEffectActive) {
        this.guthanSetEffectActive = guthanSetEffectActive;
    }

    public final boolean isGuthanSetEffectActive() {
        return this.guthanSetEffectActive;
    }

    public final void setVeracSetEffectActive(boolean veracSetEffectActive) {
        this.veracSetEffectActive = veracSetEffectActive;
    }

    public final boolean isVeracSetEffectActive() {
        return this.veracSetEffectActive;
    }

    public final void setProtectionPrayerDisabledUntil(long prayerId) {
        this.protectionPrayerDisabledUntil = prayerId;
    }

    public final long getProtectionPrayerDisabledUntil() {
        return this.protectionPrayerDisabledUntil;
    }

    @Override
    public final int getCurrentHitpoints() {
        return this.skillManager.getCurrentLevels()[3];
    }

    @Override
    public final int getMaxHitpoints() {
        return this.skillManager.getBaseLevel(3);
    }

    @Override
    public final int getDeathAnimationId() {
        if (ServerSettings.cacheVersion < 327) {
            return 836;
        }
        return 2304;
    }

    @Override
    public final int getBlockAnimationId() {
        Object container = this.equipmentManager.getContainer().getItemAt(5);
        if (container != null && ((String)(container = ItemDefinition.forId(((ItemStack)container).getId()).getName().toLowerCase())).contains("shield")) {
            return 1156;
        }
        return this.weaponProfile.getBlockAnimationId();
    }

    @Override
    public final int getDeathDelayTicks() {
        return 6;
    }

    @Override
    public final int getAttackLevelFor(CombatType combatType) {
        if (combatType == CombatType.RANGED) {
            return this.skillManager.getCurrentLevels()[4];
        }
        if (combatType == CombatType.MAGIC) {
            return this.skillManager.getCurrentLevels()[6];
        }
        return this.skillManager.getCurrentLevels()[0];
    }

    @Override
    public final int getDefenceLevelFor(CombatType combatType) {
        if (combatType == CombatType.MAGIC) {
            return this.skillManager.getCurrentLevels()[6];
        }
        return this.skillManager.getCurrentLevels()[1];
    }

    @Override
    public final boolean isProtectedFrom(CombatType combatType) {
        if (combatType == CombatType.MELEE) {
            return this.activePrayers[14];
        }
        if (combatType == CombatType.RANGED) {
            return this.activePrayers[13];
        }
        if (combatType == CombatType.MAGIC) {
            return this.activePrayers[12];
        }
        return false;
    }

    @Override
    public final void setCurrentHitpoints(int currentHitpoints) {
        this.skillManager.setCurrentLevel(3, currentHitpoints);
        this.skillManager.refreshSkill(3);
    }

    public final long getNameHash() {
        return this.nameHash;
    }

    public final void setNameHash(long nameHash) {
        this.nameHash = nameHash;
    }

    public final void setCurrentWalkableInterfaceId(int interfaceId) {
        this.currentWalkableInterfaceId = interfaceId;
    }

    public final int getCurrentWalkableInterfaceId() {
        return this.currentWalkableInterfaceId;
    }

    public final void setSpecialAttackDefinition(SpecialAttackDefinition specialAttackDefinition) {
        this.specialAttackDefinition = specialAttackDefinition;
    }

    public final SpecialAttackDefinition getSpecialAttackDefinition() {
        return this.specialAttackDefinition;
    }

    public final void setWeaponProfile(WeaponProfile weaponProfile) {
        if (weaponProfile == null) {
            weaponProfile = WeaponProfile.FISTS;
        }
        this.weaponProfile = weaponProfile;
    }

    public final WeaponProfile getWeaponProfile() {
        return this.weaponProfile;
    }

    public final int getBlockSoundId() {
        int value = 820; // human_block_1; 405 is a metal clash, even without armour.
        Player player = this;
        int itemIdAtSlot = player.equipmentManager.getItemIdAtSlot(4);
        String definition = new ItemStack(itemIdAtSlot).getDefinition().getName().toLowerCase();
        if (definition.contains("platebody")) {
            value = 410;
        }
        if (definition.contains("chainbody")) {
            value = 414;
        }
        return value;
    }

    public final int getHitSoundId() {
        return this.gender == 1 ? 73 : 69;
    }

    public final boolean handleSpecialAttackButton(int buttonId) {
        Player player;
        switch (buttonId) {
            case 7487: {
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(3) != 1377) break;
                SpecialAttackDefinition.performDragonBattleaxeSpecial(this);
                return true;
            }
            case 7587: {
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(3) != 35) break;
                SpecialAttackDefinition.performExcaliburSpecial(this);
                return true;
            }
            case 7462: {
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(3) != 4153) break;
                if (this.getCombatTarget() == null) {
                    player = this;
                    player.packetSender.sendGameMessage("You can only use this special when attacking something.");
                    return true;
                }
                SpecialAttackDefinition.performGraniteMaulSpecial(this);
                return true;
            }
        }
        boolean enabled = this.specialAttackEnabled;
        if (this.weaponProfile.getInterfaceDefinition().getSpecialAttackButtonId() != buttonId) {
            return false;
        }
        player = this;
        SpecialAttackDefinition specialAttackDefinition = player.specialAttackDefinition;
        if (specialAttackDefinition != null) {
            player = this;
            if (player.specialEnergy < specialAttackDefinition.getEnergyCost()) {
                player = this;
                player.packetSender.sendGameMessage("You don't have enough special energy to do that.");
                return true;
            }
        }
        boolean enabled2 = !this.specialAttackEnabled;
        player = this;
        this.specialAttackEnabled = enabled2;
        if (this.specialAttackEnabled && specialAttackDefinition == null) {
            enabled2 = false;
            player = this;
            this.specialAttackEnabled = enabled2;
        }
        if (this.specialAttackEnabled != enabled) {
            this.refreshSpecialAttackWidgets();
        }
        return true;
    }

    public final void refreshSpecialAttackWidgets() {
        if (this.weaponProfile.getInterfaceDefinition().getSpecialBarWidgetId() <= 0) {
            return;
        }
        this.packetSender.refreshSpecialEnergyBar(this.weaponProfile.getInterfaceDefinition().getSpecialEnergyWidgetId());
        this.weaponProfile.getInterfaceDefinition().getSpecialEnergyWidgetId();
        this.packetSender.refreshSpecialAttackConfig();
    }

    public final void clearPvpCombatReferences() {
        this.pvpCombatReferences.clear();
        this.setSkulled(false);
    }

    public final int getSkullTimer() {
        int value = this.pvpCombatReferences.size();
        if (value == 0) {
            return 0;
        }
        value = 0;
        Iterator iterator = this.pvpCombatReferences.iterator();
        while (iterator.hasNext()) {
            int remainingTicks = ((PvpCombatReference)iterator.next()).getRemainingTicks();
            if (remainingTicks <= value) continue;
            value = remainingTicks;
        }
        return value;
    }

    public final void addPvpCombatReference(Player player, int value2) {
        PvpCombatReference pvpCombatReference = new PvpCombatReference((Entity)player, value2);
        this.pvpCombatReferences.add(pvpCombatReference);
        this.setSkulled(true);
    }

    public final void recordPvpAttack(Player player) {
        if (!player.isPlayer()) {
            return;
        }
        if (this.isInDuelArena()
                || CastleWarsManager.isInGame(this)
                || CastleWarsManager.isInGame(player)) {
            return;
        }
        for (Object referenceObject : player.pvpCombatReferences) {
            if (((EntityReference)referenceObject).resolve() != this) continue;
            return;
        }
        for (Object referenceObject : this.pvpCombatReferences) {
            if (((EntityReference)referenceObject).resolve() != player) continue;
            ((PvpCombatReference)referenceObject).resetTimer();
            return;
        }
        this.addPvpCombatReference(player, 2000);
    }

    private void setSkulled(boolean skulled) {
        this.skulled = skulled;
        int value = skulled ? 0 : -1;
        Player player = this;
        this.skullIcon = value;
        this.setAppearanceUpdateRequired(true);
    }

    public final ArrayList getUnprotectedItems(ItemStack[] items) {
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        PriorityQueue<ItemStack> priorityQueue = new PriorityQueue<ItemStack>(1, new ProtectedItemValueComparator(this));
        int index = 0;
        while (index < items.length) {
            ItemStack itemStack = items[index];
            if (!(itemStack == null || itemStack.getDefinition().isUntradeable() && itemStack.getDefinition().getValue() == 1)) {
                ItemStack protectedItem = new ItemStack(itemStack.getId());
                priorityQueue.add(protectedItem);
                arrayList.add(protectedItem);
            }
            ++index;
        }
        int value = this.skulled ? 0 : 3;
        if (this.activePrayers[8]) {
            ++value;
        }
        if (this.gameMode == 2) {
            value = 0;
        }
        ArrayList<ItemStack> protectedItems = new ArrayList<ItemStack>();
        while (protectedItems.size() < value && priorityQueue.size() > 0) {
            ItemStack itemStack = (ItemStack)priorityQueue.poll();
            protectedItems.add(itemStack);
            arrayList.remove(itemStack);
        }
        return arrayList;
    }

    public final void advanceTutorialStage() {
        if (this.questStates[0] == 1) {
            return;
        }
        this.questStates[0] = this.questStates[0] + 1;
        Player player = this;
        player.questManager.refreshQuestJournal();
    }

    public final void completeTutorial() {
        this.questStates[0] = 68;
        Player player = this;
        player.questManager.refreshQuestJournal();
    }

    @Override
    public final void dropDeathItems(Entity entity) {
        if (this.playerRights >= 2 || this.isInDuelArena() || this.creatureGraveyardController.isInsideGraveyard() || this.isInFightCave()) {
            return;
        }
        if (entity == null || !(entity instanceof Player)) {
            entity = this;
        }
        ItemStack[] itemStackArray = new ItemStack[this.equipmentManager.getContainer().getCapacity() + this.inventoryManager.getContainer().getCapacity()];
        System.arraycopy(this.equipmentManager.getContainer().getItems(), 0, itemStackArray, 0, this.equipmentManager.getContainer().getItems().length);
        System.arraycopy(this.inventoryManager.getContainer().getItems(), 0, itemStackArray, this.equipmentManager.getContainer().getItems().length, this.inventoryManager.getContainer().getItems().length);
        PriorityQueue<ItemStack> priorityQueue = new PriorityQueue<ItemStack>(1, new DeathItemValueComparator(this));
        int index = 0;
        while (index < itemStackArray.length) {
            ItemStack itemStack = itemStackArray[index];
            if (!(itemStack == null || itemStack.getDefinition().isUntradeable() && itemStack.getDefinition().getValue() == 1)) {
                priorityQueue.add(new ItemStack(itemStack.getId(), itemStack.getAmount(), itemStack.getMetadata()));
            }
            ++index;
        }
        ArrayList<ItemStack> protectedItems = new ArrayList<ItemStack>();
        int value = this.skulled ? 0 : 3;
        if (this.activePrayers[8]) {
            ++value;
        }
        if (this.gameMode == 2) {
            value = 0;
        }
        index = 0;
        while (index < value && priorityQueue.size() > 0) {
            ItemStack itemStack = (ItemStack)priorityQueue.poll();
            int value2 = value - index;
            if (itemStack.getAmount() < value2) {
                value2 = itemStack.getAmount();
            }
            index += value2;
            itemStack.setAmount(value2);
            protectedItems.add(itemStack);
        }
        ArrayList<ItemStack> droppedItems = new ArrayList<ItemStack>(Arrays.asList(itemStackArray));
        dropDeathItemsControlLoop1: for (ItemStack protectedItem : protectedItems) {
            if (protectedItem == null) continue;
            Iterator iterator = droppedItems.iterator();
            while (iterator.hasNext()) {
                ItemStack itemStack = (ItemStack)iterator.next();
                if (itemStack == null || itemStack.getId() != protectedItem.getId()) continue;
                itemStack.setAmount(itemStack.getAmount() - protectedItem.getAmount());
                if (itemStack.getAmount() > 0) continue dropDeathItemsControlLoop1;
                iterator.remove();
                continue dropDeathItemsControlLoop1;
            }
        }
        this.equipmentManager.getContainer().clear();
        this.inventoryManager.getContainer().clear();
        Entity lootOwner = entity;
        if (entity.isPlayer() && entity != this) {
            Player killer = (Player)entity;
            if (killer.gameMode != 0) {
                killer.packetSender.sendGameMessage("You are not playing on normal gamemode and cannot receive the loot.");
                lootOwner = this;
            }
        }
        for (ItemStack itemStack : protectedItems) {
            this.inventoryManager.addItem(itemStack);
        }
        for (ItemStack itemStack : droppedItems) {
            if (itemStack == null) continue;
            if (itemStack.getDefinition().getName().toLowerCase().contains("clue scroll")) {
                this.treasureTrailStepCount = 0;
            }
            BarrowsRepairHandler barrowsRepairHandler = BarrowsRepairHandler.forItem(itemStack);
            if (itemStack.getDefinition().isUntradeable() && barrowsRepairHandler == null) continue;
            ItemStack groundStack = new ItemStack(itemStack.getId(), itemStack.getAmount());
            if (barrowsRepairHandler != null) {
                groundStack = new ItemStack(barrowsRepairHandler.getFullyDegradedItemId(), 1);
            }
            GroundItem groundItem = new GroundItem(groundStack, (Entity)this, lootOwner, this.getDeathPosition());
            GroundItemManager.getInstance().spawn(groundItem);
            if (!entity.isPlayer() || entity == this) continue;
            Player killer = (Player)entity;
            if (!killer.botEnabled) continue;
            killer.botLootGroundItems.add(groundItem);
        }
        if (entity.isPlayer() && entity != this) {
            Player killer = (Player)entity;
            if (killer.botEnabled) {
                if (killer.botLootGroundItems.size() > 0) {
                    killer.botCombatState = "loot items";
                    BotCombatHelper.processBotLootQueue(killer);
                } else {
                    killer.botCombatState = null;
                }
            }
        }
        boolean enabled = false;
        if (this.gameMode == 3) {
            this.gameMode = 1;
            this.packetSender.sendAccountStatus();
            enabled = true;
            this.packetSender.sendGameMessage("You have fallen as a Hardcore Iron Man, your Hardcore status has been revoked.");
        }
        CharacterFileManager.savePlayer((Player)this);
        if (enabled) {
            CharacterFileManager.archiveDeadHardcoreIronman((Player)this);
        }
        if (entity != null && this.getDeathPosition() != null) {
            GroundItem groundItem = new GroundItem(new ItemStack(526, 1), (Entity)this, entity, this.getDeathPosition());
            GroundItemManager.getInstance().spawn(groundItem);
        }
        this.equipmentManager.refresh();
        this.inventoryManager.refresh();
    }

    public final SpellDefinition getQueuedCombatSpell() {
        return this.queuedCombatSpell;
    }

    public final SpellDefinition getAutocastSpell() {
        return this.autocastSpell;
    }

    public final void setQueuedCombatSpell(SpellDefinition spellDefinition) {
        this.queuedCombatSpell = spellDefinition;
    }

    public final boolean isAutocastEnabled() {
        return this.autocastEnabled;
    }

    public final void setAutocastEnabled(boolean autocastEnabled) {
        if (ServerSettings.clientBuild == 443) {
            this.autocastEnabled = autocastEnabled && this.autocastSpell != null;
            this.packetSender.refreshAutocastConfig();
            return;
        }
        if (autocastEnabled) {
            Player player = this;
            player.packetSender.sendConfig(108, 3);
            player = this;
            player.packetSender.sendConfig(43, 3);
        } else {
            Player player = this;
            player.packetSender.refreshAutocastConfig();
        }
        this.autocastEnabled = autocastEnabled;
    }

    public final void setAutocastSpell(SpellDefinition spellDefinition) {
        if (ServerSettings.clientBuild == 443) {
            this.autocastSpell = spellDefinition;
            this.autocastEnabled = spellDefinition != null;
            if (spellDefinition != null) {
                this.packetSender.setSidebarInterface(0, 328);
            }
            this.packetSender.sendInterfaceText(spellDefinition == null ? "Spell"
                    : TextUtil.capitalizeFirst(spellDefinition.name().toLowerCase().replaceAll("_", " ")), 352);
            this.packetSender.refreshAutocastConfig();
            return;
        }
        if (spellDefinition == null) {
            Player player = this;
            player.packetSender.refreshAutocastConfig();
            this.autocastEnabled = false;
        } else {
            Object value = this;
            ((Player)value).packetSender.setSidebarInterface(0, 328);
            value = this;
            Object value2 = spellDefinition;
            value = ((Player)value).packetSender;
            value2 = TextUtil.capitalizeFirst(((SpellDefinition)value2).name().toLowerCase().replaceAll("_", " "));
            ((PacketSender)value).sendInterfaceText((String)value2, 352);
            ((PacketSender)value).sendConfig(108, 3);
            ((PacketSender)value).sendConfig(43, 3);
            this.autocastEnabled = true;
        }
        this.autocastSpell = spellDefinition;
    }

    public final void disableAutocast() {
        if (ServerSettings.clientBuild == 443) {
            this.autocastEnabled = false;
            this.packetSender.refreshAutocastConfig();
            return;
        }
        Player player = this;
        player.packetSender.sendConfig(108, 2);
        this.autocastEnabled = false;
    }

    public final void setMemberFlag(boolean memberFlag) {
        this.memberFlag = memberFlag;
    }

    public final boolean hasMemberFlag() {
        return this.memberFlag;
    }

    public final ByteBuffer getOutboundBuffer() {
        return this.outboundBuffer;
    }

    public final void setInterfaceAction(String interfaceId) {
        this.interfaceAction = interfaceId;
    }

    public final void setSelectedSkillItemId(int itemId) {
        this.selectedSkillItemId = itemId;
    }

    public final int getSelectedSkillItemId() {
        return this.selectedSkillItemId;
    }

    public final void setSelectedSmithingBarItemId(int itemId) {
        this.selectedSmithingBarItemId = itemId;
    }

    public final int getSelectedSmithingBarItemId() {
        return this.selectedSmithingBarItemId;
    }

    public final void setSelectedSmithingBarDefinition(SmithingBarDefinition smithingBarDefinition) {
        this.selectedSmithingBarDefinition = smithingBarDefinition;
    }

    public final SmithingBarDefinition getSelectedSmithingBarDefinition() {
        return this.selectedSmithingBarDefinition;
    }

    public final int getStandAnimation() {
        if (this.standAnimationOverride == -1) {
            Player player = this;
            return player.equipmentManager.getStandAnimation();
        }
        return this.standAnimationOverride;
    }

    public final int getWalkAnimation() {
        if (this.walkAnimationOverride == -1) {
            Player player = this;
            return player.equipmentManager.getWalkAnimation();
        }
        return this.walkAnimationOverride;
    }

    public final int getRunAnimation() {
        if (this.runAnimationOverride == -1) {
            Player player = this;
            return player.equipmentManager.getRunAnimation();
        }
        return this.runAnimationOverride;
    }

    public final void setRunAnimationOverride(int animationId) {
        this.runAnimationOverride = animationId;
    }

    public final void setWalkAnimationOverride(int animationId) {
        this.walkAnimationOverride = animationId;
    }

    public final void setStandAnimationOverride(int animationId) {
        this.standAnimationOverride = animationId;
    }

    public final void setMuteExpires(long muteExpires) {
        this.muteExpires = muteExpires;
    }

    public final void setBanExpires(long banExpires) {
        this.banExpires = banExpires;
    }

    public final long getMuteExpires() {
        return this.muteExpires;
    }

    public final long getBanExpires() {
        return this.banExpires;
    }

    public final void resetCombatState() {
        Object value = this;
        ((Player)value).pvpCombatReferences.clear();
        this.setSkulled(false);
        Player player = this;
        player.prayerManager.deactivateAll();
        this.setRunEnergyPercent(100);
        this.setSpecialEnergy(100);
        this.refreshSpecialAttackWidgets();
        this.clearNegativeStatusTimers();
        this.clearImmunityTimers();
        player = this;
        value = player.skillManager.getCurrentLevels();
        int index = 0;
        while (index < ((int[])value).length) {
            Player player2 = this;
            player = player2;
            player = this;
            player2.skillManager.setCurrentLevel(index, player.skillManager.getBaseLevel(index));
            ++index;
        }
    }

    public final boolean isInterfaceIdOpen(int interfaceId) {
        // Native cooking child 307:3 translates to legacy Cook All (13717).
        // The same legacy ID writes food-name text to native child 307:1,
        // but this action must only be accepted when the cooking chatbox is open.
        if (ServerSettings.clientBuild == 443
                && interfaceId == 13717
                && this.openInterfaceId == 1743) {
            return true;
        }
        if (interfaceId >= 19600 && interfaceId <= 19640) {
            return this.openInterfaceId == WorldTeleportMenu.ROOT_INTERFACE_ID;
        }
        InterfaceDefinition interfaceDefinition = InterfaceDefinition.forId(interfaceId);
        if (interfaceDefinition != null && this.isInterfaceOpen(interfaceDefinition)) {
            return true;
        }
        // The server keeps legacy interface roots open (for example 3559),
        // while revision 443 displays their mapped native groups (269). A
        // legacy component definition can therefore have a parent that does
        // not match the stored root even though its mapped 443 group is open.
        // Do not return false early; let the bridge validate that mapping.
        if (ServerSettings.clientBuild == 443) {
            Integer packedComponentId = InterfaceBridge.componentMappings().get(interfaceId);
            if (packedComponentId != null) {
                int mappedGroupId = packedComponentId >> 16;
                Map<Integer, Integer> groupMappings = InterfaceBridge.groupMappings();
                if (Integer.valueOf(mappedGroupId).equals(groupMappings.get(this.openInterfaceId))
                        || Integer.valueOf(mappedGroupId).equals(groupMappings.get(this.inventoryOverlayInterfaceId))) {
                    return true;
                }
                for (int sidebarInterfaceId : this.sidebarInterfaceIds) {
                    if (Integer.valueOf(mappedGroupId).equals(groupMappings.get(sidebarInterfaceId))) {
                        return true;
                    }
                }
            }
        }
        if (interfaceId >= 18890 && interfaceId <= 19102) {
            return this.openInterfaceId >= 18890 && this.openInterfaceId <= 19102
                || this.inventoryOverlayInterfaceId >= 18890 && this.inventoryOverlayInterfaceId <= 19102;
        }
        return false;
    }

    public final boolean isInterfaceOpen(InterfaceDefinition interfaceDefinition) {
        if (interfaceDefinition == null) {
            return false;
        }
        Player player = this;
        if (player.openInterfaceId == interfaceDefinition.getParentInterfaceId()) {
            return true;
        }
        if (this.inventoryOverlayInterfaceId == interfaceDefinition.getParentInterfaceId()) {
            return true;
        }
        int[] integerValues = this.sidebarInterfaceIds;
        int length = this.sidebarInterfaceIds.length;
        int index = 0;
        while (index < length) {
            int value = integerValues[index];
            if (value == interfaceDefinition.getParentInterfaceId()) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final void logError(String text2) {
        Object value = "./data/errors.txt";
        try {
            value = new BufferedWriter(new FileWriter((String)value, true));
            try {
                Player player = this;
                ((Writer)value).write("[" + player.username + "] " + text2);
                ((BufferedWriter)value).newLine();
            }
            finally {
                ((BufferedWriter)value).close();
            }
            return;
        }
        catch (IOException iOException) {
            value = iOException;
            iOException.printStackTrace();
            return;
        }
    }

    public final void resetInteractionState() {
        this.invalidateInterruptibleAction();
        this.nextActionSequence();
        this.interactionApproachX = 0;
        this.interactionApproachY = 0;
        this.interactionOffsetX = 0;
        this.interactionOffsetY = 0;
        this.interactionObjectSizeX = 0;
        this.interactionObjectSizeY = 0;
        this.interactionExitX = 0;
        this.interactionExitY = 0;
        this.interactionExitPlane = 0;
        Player player = this;
        int initialValue = -1;
        Player player2 = player;
        player.selectedItemInterfaceId = initialValue;
        initialValue = -1;
        player2 = player;
        player.interactionTargetId = initialValue;
        initialValue = -1;
        player2 = player;
        player.interactionTargetY = initialValue;
        initialValue = -1;
        player2 = player;
        player.selectedItemSlot = initialValue;
        initialValue = -1;
        player2 = player;
        player.interactionTargetX = initialValue;
        initialValue = -1;
        player2 = player;
        player.interactionTargetPlane = initialValue;
        initialValue = -1;
        player2 = player;
        player.selectedItemId = initialValue;
        EntityTargetMovement.clearMovementTarget(this);
        if (this.getInteractionTarget() != null && this.getInteractionTarget().isNpc()) {
            this.getInteractionTarget().setInteractionTarget(null);
        }
        this.setInteractionTarget(null);
        CombatManager.stopCombat(this);
        player2 = this;
        player2.dialogueManager.finishDialogue();
        if (this.getUpdateState().getFaceEntityId() != 65535) {
            this.getUpdateState().setFaceEntity(65535);
        }
    }

    public final boolean isInsideArdougneZooMonkeyEnclosure() {
        if (ardougneZooMonkeyEnclosure == null) {
            ardougneZooMonkeyEnclosure = new Polygon();
            ardougneZooMonkeyEnclosure.addPoint(2600, 3281);
            ardougneZooMonkeyEnclosure.addPoint(2600, 3279);
            ardougneZooMonkeyEnclosure.addPoint(2598, 3277);
            ardougneZooMonkeyEnclosure.addPoint(2598, 3275);
            ardougneZooMonkeyEnclosure.addPoint(2599, 3274);
            ardougneZooMonkeyEnclosure.addPoint(2602, 3276);
            ardougneZooMonkeyEnclosure.addPoint(2604, 3276);
            ardougneZooMonkeyEnclosure.addPoint(2606, 3278);
            ardougneZooMonkeyEnclosure.addPoint(2606, 3280);
            ardougneZooMonkeyEnclosure.addPoint(2604, 3282);
            ardougneZooMonkeyEnclosure.addPoint(2601, 3282);
        }
        return ardougneZooMonkeyEnclosure.contains(this.getPosition().getX(), this.getPosition().getY());
    }

    public final boolean shouldHideHeldItemsInAppearance() {
        return this.hideHeldItemsInAppearance;
    }

    /** Respect activity-specific suppression of weapons and shields. */
    public final boolean shouldHideEquipmentItemInAppearance(int slot) {
        return (slot == 3 || slot == 5) && this.hideHeldItemsInAppearance;
    }

    public final void setHideHeldItemsInAppearance(boolean itemId) {
        this.hideHeldItemsInAppearance = itemId;
    }

    public final boolean isVisibleToOtherPlayers() {
        return this.visibleToOtherPlayers;
    }

    public final boolean hasFullVoidMagicSet() {
        Player player = this;
        if (player.equipmentManager.getItemIdAtSlot(9) == 8842) {
            player = this;
            if (player.equipmentManager.getItemIdAtSlot(7) == 8840) {
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(4) == 8839) {
                    player = this;
                    if (player.equipmentManager.getItemIdAtSlot(0) == 11663) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public final boolean hasFullVoidRangedSet() {
        Player player = this;
        if (player.equipmentManager.getItemIdAtSlot(9) == 8842) {
            player = this;
            if (player.equipmentManager.getItemIdAtSlot(7) == 8840) {
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(4) == 8839) {
                    player = this;
                    if (player.equipmentManager.getItemIdAtSlot(0) == 11664) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public final boolean hasFullVoidMeleeSet() {
        Player player = this;
        if (player.equipmentManager.getItemIdAtSlot(9) == 8842) {
            player = this;
            if (player.equipmentManager.getItemIdAtSlot(7) == 8840) {
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(4) == 8839) {
                    player = this;
                    if (player.equipmentManager.getItemIdAtSlot(0) == 11665) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public final int getDragonfireProtectionState() {
        boolean enabled;
        boolean enabled2;
        boolean enabled3;
        int value;
        getDragonfireProtectionStateControlExit1: {
            getDragonfireProtectionStateControlExit2: {
                value = 0;
                enabled3 = false;
                enabled2 = false;
                enabled = false;
                if (this.isAntifireActive()) {
                    enabled3 = true;
                }
                Player player = this;
                if (player.activePrayers[12]) {
                    enabled2 = true;
                }
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(5) == 1540) break getDragonfireProtectionStateControlExit2;
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(5) == 11283) break getDragonfireProtectionStateControlExit2;
                player = this;
                if (player.equipmentManager.getItemIdAtSlot(5) != 11284) break getDragonfireProtectionStateControlExit1;
            }
            enabled = true;
        }
        if (enabled3 && !enabled2 && !enabled) {
            value = 1;
        }
        if (!enabled3 && enabled2 && !enabled) {
            value = 2;
        }
        if (!enabled3 && !enabled2 && enabled) {
            value = 3;
        }
        if (enabled3 && enabled2 && !enabled) {
            value = 4;
        }
        if (enabled3 && !enabled2 && enabled) {
            value = 5;
        }
        if (!enabled3 && enabled2 && enabled) {
            value = 6;
        }
        if (enabled3 && enabled2 && enabled) {
            value = 7;
        }
        return value;
    }

    public final boolean isWearingMonkeyDisguise() {
        Player player = this;
        if (player.equipmentManager.getItemIdAtSlot(3) == 4024) {
            player = this;
            if (player.equipmentManager.getItemIdAtSlot(2) == 4021) {
                return true;
            }
        }
        return false;
    }

    public final int getEssencePouchAmount(int amount) {
        return this.essencePouchAmounts[amount];
    }

    public final void setEssencePouchAmount(int amount, int value22) {
        this.essencePouchAmounts[amount] = value22;
    }

    public final void setClientBuild(int clientBuild) {
        this.clientBuild = clientBuild;
    }

    public final void setLoginMagicByte(int loginMagicByte) {
        this.loginMagicByte = loginMagicByte;
    }

    public final void setAbyssMageNpcId(int npcId) {
        this.abyssMageNpcId = npcId;
    }

    public final int getAbyssMageNpcId() {
        return this.abyssMageNpcId;
    }

    public final boolean hasRestrictedCombatEquipment() {
        int value;
        int value2;
        int[] integerValues;
        Object value3 = this;
        ItemStack[] itemStackArray = ((Player)value3).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value3 = itemStackArray[index];
            if (value3 != null && ((ItemStack)value3).getDefinition().getEquipmentSlot() != 2 && ((ItemStack)value3).getDefinition().getEquipmentSlot() != 12 && ((ItemStack)value3).getDefinition().getEquipmentSlot() != 13) {
                integerValues = ((ItemStack)value3).getDefinition().getBonuses();
                value2 = integerValues.length;
                value = 0;
                while (value < value2) {
                    int value4 = integerValues[value];
                    if (value4 > 0) {
                        return true;
                    }
                    ++value;
                }
            }
            ++index;
        }
        value3 = this;
        itemStackArray = ((Player)value3).equipmentManager.getContainer().getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            ItemStack itemStack = itemStackArray[index];
            if (itemStack != null && itemStack.getDefinition().getEquipmentSlot() != 2 && itemStack.getDefinition().getEquipmentSlot() != 12 && itemStack.getDefinition().getEquipmentSlot() != 13) {
                integerValues = itemStack.getDefinition().getBonuses();
                value2 = integerValues.length;
                value = 0;
                while (value < value2) {
                    int value5 = integerValues[value];
                    if (value5 > 0) {
                        return true;
                    }
                    ++value;
                }
            }
            ++index;
        }
        return false;
    }

    public final boolean depositInventoryAndEquipment() {
        return BankManager.depositInventoryAndEquipment(this);
    }

    public final RectangularArea getLocalViewArea() {
        return this.localViewArea;
    }

    public final void refreshLocalViewArea() {
        Object value = this;
        ((Entity)value).getPosition();
        int value2 = Position.updateLocalX((Player)value);
        ((Entity)value).getPosition();
        int value3 = Position.updateLocalY((Player)value);
        value2 = ((Entity)value).getPosition().getX() - value2;
        value3 = ((Entity)value).getPosition().getY() - value3;
        value = new Position(value2, value3, ((Entity)value).getPosition().getPlane());
        this.localViewArea = RectangularArea.fromPositionOffset((Position)value, 104, 104);
    }

    public final boolean ownsItem(int itemId) {
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getId() == itemId) {
                return true;
            }
            ++index;
        }
        value = this;
        itemStackArray = ((Player)value).bankContainer.getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getId() == itemId) {
                return true;
            }
            ++index;
        }
        value = this;
        itemStackArray = ((Player)value).equipmentManager.getContainer().getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getId() == itemId) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean ownsItemAmount(int itemId, int value3) {
        int index = 0;
        Object value2 = this;
        ItemStack[] itemStackArray = ((Player)value2).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index2 = 0;
        while (index2 < length) {
            value2 = itemStackArray[index2];
            if (value2 != null && ((ItemStack)value2).getId() == itemId) {
                index += ((ItemStack)value2).getAmount();
            }
            ++index2;
        }
        value2 = this;
        itemStackArray = ((Player)value2).bankContainer.getItems();
        length = itemStackArray.length;
        index2 = 0;
        while (index2 < length) {
            value2 = itemStackArray[index2];
            if (value2 != null && ((ItemStack)value2).getId() == itemId) {
                index += ((ItemStack)value2).getAmount();
            }
            ++index2;
        }
        value2 = this;
        itemStackArray = ((Player)value2).equipmentManager.getContainer().getItems();
        length = itemStackArray.length;
        index2 = 0;
        while (index2 < length) {
            value2 = itemStackArray[index2];
            if (value2 != null && ((ItemStack)value2).getId() == itemId) {
                index += ((ItemStack)value2).getAmount();
            }
            ++index2;
        }
        return index >= value3;
    }

    public final int getOwnedItemAmount(int itemId) {
        int index = 0;
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index2 = 0;
        while (index2 < length) {
            value = itemStackArray[index2];
            if (value != null && ((ItemStack)value).getId() == itemId) {
                index += ((ItemStack)value).getAmount();
            }
            ++index2;
        }
        value = this;
        itemStackArray = ((Player)value).bankContainer.getItems();
        length = itemStackArray.length;
        index2 = 0;
        while (index2 < length) {
            value = itemStackArray[index2];
            if (value != null && ((ItemStack)value).getId() == itemId) {
                index += ((ItemStack)value).getAmount();
            }
            ++index2;
        }
        value = this;
        itemStackArray = ((Player)value).equipmentManager.getContainer().getItems();
        length = itemStackArray.length;
        index2 = 0;
        while (index2 < length) {
            value = itemStackArray[index2];
            if (value != null && ((ItemStack)value).getId() == itemId) {
                index += ((ItemStack)value).getAmount();
            }
            ++index2;
        }
        return index;
    }

    public final void resetMageTrainingArenaPizazzPoints() {
        Player player = this;
        this.telekineticTheatreController.pizazzPoints = 0;
        player = this;
        this.enchantmentChamberController.pizazzPoints = 0;
        player = this;
        this.alchemistPlaygroundController.pizazzPoints = 0;
        player = this;
        this.creatureGraveyardController.pizazzPoints = 0;
    }

    public final boolean ownsProgressHat() {
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().contains("progress hat")) {
                return true;
            }
            ++index;
        }
        value = this;
        itemStackArray = ((Player)value).bankContainer.getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().contains("progress hat")) {
                return true;
            }
            ++index;
        }
        value = this;
        itemStackArray = ((Player)value).equipmentManager.getContainer().getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().contains("progress hat")) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean hasActiveProgressHat() {
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().contains("progress hat")) {
                return true;
            }
            ++index;
        }
        value = this;
        itemStackArray = ((Player)value).equipmentManager.getContainer().getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().contains("progress hat")) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final int getActiveCaveLightLevel() {
        int index = 0;
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index2 = 0;
        while (index2 < length) {
            value = itemStackArray[index2];
            if (value != null && ((ItemStack)value).getDefinition().getEquipmentSlot() == -1) {
                index += GameplayHelper.getCaveLightLevelForItemId(((ItemStack)value).getId());
            }
            ++index2;
        }
        value = this;
        itemStackArray = ((Player)value).equipmentManager.getContainer().getItems();
        length = itemStackArray.length;
        index2 = 0;
        while (index2 < length) {
            value = itemStackArray[index2];
            if (value != null && ((ItemStack)value).getDefinition().getEquipmentSlot() != -1) {
                index += GameplayHelper.getCaveLightLevelForItemId(((ItemStack)value).getId());
            }
            ++index2;
        }
        return index;
    }

    public final ItemStack findLitCaveLightSource() {
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            int value2;
            CaveLightSourceDefinition caveLightSourceDefinition;
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getEquipmentSlot() == -1 && ((caveLightSourceDefinition = CaveLightSourceDefinition.forItemId(value2 = ((ItemStack)value).getId())) == null ? false : (caveLightSourceDefinition.getUnlitItemId() == value2 ? false : caveLightSourceDefinition.canFlareInSwampGas()))) {
                return (ItemStack)value;
            }
            ++index;
        }
        return null;
    }

    public final boolean ownsClueScroll() {
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().contains("clue scroll")) {
                return true;
            }
            ++index;
        }
        value = this;
        itemStackArray = ((Player)value).bankContainer.getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().contains("clue scroll")) {
                return true;
            }
            ++index;
        }
        return false;
    }

    private boolean hasEquippedItemNamePrefix(String itemId) {
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).equipmentManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && ((ItemStack)value).getDefinition().getName().toLowerCase().startsWith(itemId)) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final boolean hasChargedAmuletOfGloryEquipped() {
        if (ServerSettings.freeToPlayWorld || !this.isMember()) {
            return false;
        }
        Player player = this;
        if (player.equipmentManager.getContainer().getItemAt(2) == null) {
            return false;
        }
        player = this;
        int container = player.equipmentManager.getContainer().getItemAt(2).getId();
        return container >= 1706 && container <= 1712 || container >= 10354 && container <= 10360;
    }

    public final boolean ownsCluePuzzleBox() {
        Object value = this;
        ItemStack[] itemStackArray = ((Player)value).inventoryManager.getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && (((ItemStack)value).getId() == 2800 || ((ItemStack)value).getId() == 3565 || ((ItemStack)value).getId() == 3571)) {
                return true;
            }
            ++index;
        }
        value = this;
        itemStackArray = ((Player)value).bankContainer.getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value = itemStackArray[index];
            if (value != null && (((ItemStack)value).getId() == 2800 || ((ItemStack)value).getId() == 3565 || ((ItemStack)value).getId() == 3571)) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public final List getVisibleGroundItems() {
        return this.visibleGroundItems;
    }

    public final void setCookingObjectId(int objectId) {
        this.cookingObjectId = objectId;
    }

    public final int getCookingObjectId() {
        return this.cookingObjectId;
    }

    public final boolean isMuted() {
        return this.muteExpires != 0L && this.muteExpires > System.currentTimeMillis();
    }

    public final boolean isBanned() {
        return this.banExpires != 0L && this.banExpires > System.currentTimeMillis();
    }

    public final int getOpenInterfaceId() {
        return this.openInterfaceId;
    }

    public final void setOpenInterfaceId(int interfaceId) {
        this.openInterfaceId = interfaceId;
    }

    public final void setBarrowsBrotherKilled(int value2, boolean enabled2) {
        this.barrowsKilledBrothers[value2] = enabled2;
    }

    public final boolean[] getBarrowsKilledBrothers() {
        return this.barrowsKilledBrothers;
    }

    public final boolean isBarrowsBrotherKilled(int value2) {
        return this.barrowsKilledBrothers[value2];
    }

    public final void setBarrowsKillCount(int skillId) {
        this.barrowsKillCount = skillId;
    }

    public final int getBarrowsKillCount() {
        return this.barrowsKillCount;
    }

    public final void setBarrowsTargetBrotherIndex(int index) {
        this.barrowsTargetBrotherIndex = index;
    }

    public final int getBarrowsTargetBrotherIndex() {
        return this.barrowsTargetBrotherIndex;
    }

    public final int getCombatLevel() {
        return this.combatLevel;
    }

    public final void setCombatLevel(int level) {
        this.combatLevel = level;
    }

    public final void setTradeRequestTarget(Player player) {
        this.tradeRequestTarget = player;
    }

    public final Player getTradeRequestTarget() {
        return this.tradeRequestTarget;
    }

    public final void setInteractionTargetPlane(int plane) {
        this.interactionTargetPlane = plane;
    }

    public final int getInteractionTargetPlane() {
        return this.interactionTargetPlane;
    }

    public final void setSidebarInterfaceId(int interfaceId, int value2) {
        this.sidebarInterfaceIds[interfaceId] = value2;
    }

    public final int getSidebarInterfaceId(int tab) {
        return this.sidebarInterfaceIds[tab];
    }

    /** The client's "Classic interface" setting: 2004 Game Options and Player controls tabs. */
    public boolean classicSidebar;

    public final void setInventoryOverlayInterfaceId(int interfaceId) {
        this.inventoryOverlayInterfaceId = interfaceId;
    }

    public final void setSelectedItemInterfaceId(int interfaceId) {
        this.selectedItemInterfaceId = interfaceId;
    }

    public final int getSelectedItemInterfaceId() {
        return this.selectedItemInterfaceId;
    }

    public final void setSelectedItemSlot(int itemId) {
        this.selectedItemSlot = itemId;
    }

    public final int getSelectedItemSlot() {
        return this.selectedItemSlot;
    }

    public final void setBrimhavenOpen(boolean brimhavenOpen) {
        this.brimhavenOpen = brimhavenOpen;
    }

    public final boolean isBrimhavenOpen() {
        return this.brimhavenOpen;
    }

    public final void setTeleotherDestination(Position position) {
        this.teleotherDestination = position;
    }

    public final Position getTeleotherDestination() {
        return this.teleotherDestination;
    }

    public final void setPendingDestroyItem(ItemStack itemStack) {
        this.pendingDestroyItem = itemStack;
    }

    public final ItemStack getPendingDestroyItem() {
        return this.pendingDestroyItem;
    }

    public final void setBankPinReminderShown(boolean bankPinReminderShown) {
        this.bankPinReminderShown = true;
    }

    public final boolean isBankPinReminderShown() {
        return this.bankPinReminderShown;
    }

    public final void setBankPinEntryDigit(int value3, int value22) {
        this.bankPinEntryDigits[value22] = value3;
    }

    public final void resetBankPinEntryDigits() {
        this.bankPinEntryDigits = new int[4];
    }

    public final int[] getBankPinEntryDigits() {
        return this.bankPinEntryDigits;
    }

    public final void setActiveRandomEventNpc(Npc npc) {
        this.activeRandomEventNpc = npc;
    }

    public final Npc getActiveRandomEventNpc() {
        return this.activeRandomEventNpc;
    }

    public final int getBossPetUnlockFlags() {
        return this.bossPetUnlockFlags;
    }

    public final void setBossPetUnlockFlags(int bossPetUnlockFlags) {
        this.bossPetUnlockFlags = bossPetUnlockFlags;
    }

    public final void setRandomEventRequestedItem(ItemStack itemStack) {
        this.randomEventRequestedItem = itemStack;
    }

    public final ItemStack getRandomEventRequestedItem() {
        return this.randomEventRequestedItem;
    }

    public final void setSelectedLampSkill(int skillId) {
        this.selectedLampSkill = skillId;
    }

    public final int getSelectedLampSkill() {
        return this.selectedLampSkill;
    }

    public final long getDisconnectGraceExpiresAtMillis() {
        return this.disconnectGraceExpiresAtMillis;
    }

    public final void setCoalTruckCoalCount(int coalTruckCoalCount) {
        this.coalTruckCoalCount = coalTruckCoalCount;
    }

    public final int getCoalTruckCoalCount() {
        return this.coalTruckCoalCount;
    }

    public final void setDuelRequestTarget(Player player) {
        this.duelRequestTarget = player;
    }

    public final Player getDuelRequestTarget() {
        return this.duelRequestTarget;
    }

    public final int getIdlePacketCount() {
        return this.idlePacketCount;
    }

    public final void setIdlePacketCount(int packetId) {
        this.idlePacketCount = packetId;
    }
}
