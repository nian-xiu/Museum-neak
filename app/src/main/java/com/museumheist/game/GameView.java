package com.museumheist.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.museumheist.game.GameState.HotbarType;
import com.museumheist.game.character.CharacterConfig;
import com.museumheist.game.character.CharacterRepository;
import com.museumheist.game.character.SelectedCharacterStore;
import com.museumheist.game.entity.Coin;
import com.museumheist.game.entity.Disruptor;
import com.museumheist.game.entity.Door;
import com.museumheist.game.entity.Guard;
import com.museumheist.game.entity.KeyItem;
import com.museumheist.game.entity.Laser;
import com.museumheist.game.entity.Player;
import com.museumheist.game.entity.PowerUp;
import com.museumheist.game.entity.SecurityCamera;
import com.museumheist.game.entity.Guard.AlertState;
import com.museumheist.game.entity.Guard.Kind;
import com.museumheist.game.entity.PowerUp.Type;
import com.museumheist.game.input.TouchCommandQueue;
import com.museumheist.game.input.VirtualJoystick;
import com.museumheist.game.logic.Collision;
import com.museumheist.game.logic.StealthTracker;
import com.museumheist.game.logic.Vision;
import com.museumheist.game.logic.StealthTracker.ThreatBand;
import com.museumheist.game.progress.LoadoutSelection;
import com.museumheist.game.progress.ProgressStore;
import com.museumheist.game.progress.UpgradeType;
import com.museumheist.game.progress.LoadoutSelection.ToggleResult;
import com.museumheist.game.render.ActorRenderer;
import com.museumheist.game.render.CharacterRenderer;
import com.museumheist.game.render.EnvironmentRenderer;
import com.museumheist.game.render.HudRenderer;
import com.museumheist.game.render.IconRenderer;
import com.museumheist.game.render.Palette;
import com.museumheist.game.spawn.ItemSpawnSystem;
import com.museumheist.game.ui.FloatingText;
import com.museumheist.game.world.Level;
import com.museumheist.game.world.LevelManager;
import com.museumheist.game.world.Wall;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable, HudRenderer.TextPainter {
   private static final String TAG = "MuseumHeistGameView";
   private static final boolean USE_ACTOR_RENDERER = true;
   private static final int ACTIVE_FRAME_RATE = 60;
   private static final int MENU_FRAME_RATE = 30;
   private static final PowerUp.Type[] SHOP_TYPES;
   private final SurfaceHolder holder;
   private final Paint paint = new Paint(1);
   private final Path visionPath = new Path();
   private final Path scenePath = new Path();
   private final Path scratchStarPath = new Path();
   private final RectF sceneRect = new RectF();
   private final RectF sceneRect2 = new RectF();
   private final RectF titleStartButton = new RectF();
   private final RectF titleCharacterButton = new RectF();
   private final RectF titleSelectButton = new RectF();
   private final RectF titleShopButton = new RectF();
   private final RectF titleSettingsButton = new RectF();
   private final RectF titleProgressPanel = new RectF();
   private final RectF titleFooterPanel = new RectF();
   private final RectF titleMapPreviewButton = new RectF();
   private final RectF titleMapPrevButton = new RectF();
   private final RectF titleMapNextButton = new RectF();
   private final RectF mapConfirmPanel = new RectF();
   private final RectF mapConfirmCancelButton = new RectF();
   private final RectF mapConfirmStartButton = new RectF();
   private final List loadoutItemButtons = new ArrayList();
   private final RectF shopBackButton = new RectF();
   private final List shopItemButtons = new ArrayList();
   private final RectF settingsBackButton = new RectF();
   private final RectF routeToggleButton = new RectF();
   private final RectF contrastToggleButton = new RectF();
   private final RectF soundToggleButton = new RectF();
   private final RectF hapticToggleButton = new RectF();
   private final RectF reduceMotionToggleButton = new RectF();
   private final RectF largeTextToggleButton = new RectF();
   private final RectF progressResetButton = new RectF();
   private final RectF selectBackButton = new RectF();
   private final RectF characterBackButton = new RectF();
   private final List characterCardButtons = new ArrayList();
   private final RectF homeButton = new RectF();
   private final RectF pauseButton = new RectF();
   private final RectF restartButton = new RectF();
   private final RectF boostButton = new RectF();
   private final RectF itemButton = new RectF();
   private final RectF inventoryPanel = new RectF();
   private final RectF timerPanel = new RectF();
   private final List powerUpButtons = new ArrayList();
   private final List hotbarSlots = new ArrayList();
   private final RectF nextButton = new RectF();
   private final RectF overlayActionButton = new RectF();
   private final RectF hudPanel = new RectF();
   private final RectF miniMapPanel = new RectF();
   private final RectF joystickTouchZone = new RectF();
   private final List levelButtons = new ArrayList();
   private final List<PowerUp> powerUps = new ArrayList<>();
   private final List<Coin> coins = new ArrayList<>();
   private final List<Disruptor> disruptors = new ArrayList<>();
   private final PointF decoyPoint = new PointF();
   private final Random random = new Random();
   private final LevelManager levelManager = new LevelManager();
   private final Player player = new Player(20.0F, 305.0F);
   private final GameState state = new GameState();
   private final VirtualJoystick joystick = new VirtualJoystick(1.0F, 0.12F);
   private final GameFeedback feedback = new GameFeedback();
   private final HudRenderer hudRenderer = new HudRenderer();
   private final IconRenderer iconRenderer = new IconRenderer();
   private final ActorRenderer actorRenderer;
   private final CharacterRenderer characterRenderer;
   private final EnvironmentRenderer environmentRenderer = new EnvironmentRenderer();
   private final ItemSpawnSystem itemSpawnSystem = new ItemSpawnSystem();
   private final StealthTracker stealthTracker = new StealthTracker();
   private final FloatingText floatingText = new FloatingText();
   private final LoadoutSelection loadoutSelection = new LoadoutSelection();
   private final TouchCommandQueue touchCommandQueue = new TouchCommandQueue(256);
   private final TouchCommandQueue.Command touchCommand = new TouchCommandQueue.Command();
   private final int[] bestStars;
   private final int[] shopStock;
   private final int[] upgradeLevels;
   private final int[] runStashLoaded;
   private final Object renderThreadLock;
   private final Object gameStateLock;
   private final LevelResult levelResult;
   private final ProgressStore progressStore;
   private final SelectedCharacterStore selectedCharacterStore;
   private AppScreen appScreen;
   private CharacterConfig selectedCharacter;
   private Thread renderThread;
   private volatile boolean running;
   private volatile boolean activityResumed;
   private volatile boolean surfaceReady;
   private boolean sessionInitialized;
   private boolean pausedByLifecycle;
   private long lastBackPressMillis;
   private long lastFrameTime;
   private int screenWidth;
   private int screenHeight;
   private float uiScale;
   private float worldScale;
   private float cameraX;
   private float cameraY;
   private float uiElapsedSeconds;
   private float levelElapsedSeconds;
   private float transitionSeconds;
   private float messageSeconds;
   private String messageText;
   private int messageColor;
   private float nextPowerUpSeconds;
   private int coinsBalance;
   private int runCoinTotal;
   private float treasureClaimSeconds;
   private int treasureClaimIndex;
   private float decoySeconds;
   private int boostPointerId;
   private boolean showPatrolRoutes;
   private boolean highContrastVision;
   private boolean soundFeedbackEnabled;
   private boolean hapticFeedbackEnabled;
   private boolean reduceMotion;
   private boolean largeTextMode;
   private float progressResetConfirmSeconds;
   private boolean exitPromptVisible;
   private boolean exitPromptNeedsTreasure;
   private boolean exitPromptSuppressedUntilLeave;
   private boolean mapConfirmVisible;
   private boolean transitionVisible;
   private boolean transitionAdvanceLevel;
   private boolean transitionRestartCampaign;
   private boolean transitionStartingFromHome;
   private int pendingStartLevelIndex;
   private String transitionTitle;
   private String transitionSubtitle;
   private float exitStaySeconds;
   private final RectF exitPromptPanel;
   private final RectF exitConfirmButton;
   private final RectF exitContinueButton;

   public GameView(Context context) {
      super(context);
      this.characterRenderer = new CharacterRenderer(context.getResources());
       this.actorRenderer = new ActorRenderer(context.getResources());
      this.bestStars = new int[this.levelManager.getLevelCount()];
      this.shopStock = new int[Type.values().length];
      this.upgradeLevels = new int[UpgradeType.values().length];
      this.runStashLoaded = new int[Type.values().length];
      this.renderThreadLock = new Object();
      this.gameStateLock = new Object();
      this.levelResult = new LevelResult();
      this.appScreen = GameView.AppScreen.TITLE;
      this.selectedCharacter = CharacterRepository.getDefault();
      this.screenWidth = 1;
      this.screenHeight = 1;
      this.uiScale = 1.0F;
      this.worldScale = 1.0F;
      this.messageText = "";
      this.messageColor = -1;
      this.treasureClaimIndex = -1;
      this.boostPointerId = -1;
      this.showPatrolRoutes = true;
      this.soundFeedbackEnabled = true;
      this.hapticFeedbackEnabled = true;
      this.pendingStartLevelIndex = -1;
      this.transitionTitle = "";
      this.transitionSubtitle = "";
      this.exitPromptPanel = new RectF();
      this.exitConfirmButton = new RectF();
      this.exitContinueButton = new RectF();
      this.progressStore = new ProgressStore(context, this.levelManager.getLevelCount());
      this.selectedCharacterStore = new SelectedCharacterStore(context);
      this.holder = this.getHolder();
      this.holder.addCallback(this);
      this.setFocusable(true);
      this.setHapticFeedbackEnabled(true);
      this.setSoundEffectsEnabled(true);
      this.paint.setTypeface(Typeface.create("sans-serif", 0));
      this.loadProgress();
   }

   public void resume() {
      this.activityResumed = true;
      synchronized(this.gameStateLock) {
         if (this.pausedByLifecycle) {
            this.pausedByLifecycle = false;
            this.showMessage("已从后台返回，行动保持暂停。", Color.rgb(104, 196, 202));
         }
      }

      this.startThreadIfNeeded();
   }

   public void pause() {
      this.activityResumed = false;
      this.stopRenderThread();
      synchronized(this.gameStateLock) {
         if (this.appScreen == GameView.AppScreen.PLAYING && this.state.isPlaying()) {
            this.state.pause();
            this.joystick.reset();
            this.boostPointerId = -1;
            this.pausedByLifecycle = true;
         }

         this.touchCommandQueue.clear();
      }

      this.feedback.release();
   }

   public void shutdown() {
      this.activityResumed = false;
      this.surfaceReady = false;
      this.stopRenderThread();
      synchronized(this.gameStateLock) {
         this.touchCommandQueue.clear();
         this.joystick.reset();
         this.boostPointerId = -1;
      }

      this.feedback.release();
   }

   public void surfaceCreated(SurfaceHolder surfaceHolder) {
      this.surfaceReady = true;
      synchronized(this.gameStateLock) {
         this.configureViewport(this.getWidth(), this.getHeight());
         if (!this.sessionInitialized) {
            this.showTitleScreen();
            this.sessionInitialized = true;
         } else {
            this.updateCamera(0.0F, true);
         }
      }

      this.startThreadIfNeeded();
   }

   public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
      synchronized(this.gameStateLock) {
         this.configureViewport(width, height);
         this.updateCamera(0.0F, true);
      }
   }

   public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
      this.surfaceReady = false;
      this.stopRenderThread();
   }

   public void run() {
      Thread currentThread = Thread.currentThread();
      this.lastFrameTime = System.nanoTime();

      try {
         while(this.shouldRender(currentThread)) {
            long frameStart = System.nanoTime();
            float dt = Math.min((float)(frameStart - this.lastFrameTime) / 1.0E9F, 0.05F);
            this.lastFrameTime = frameStart;
            int frameRate;
            synchronized(this.gameStateLock) {
               this.processTouchCommands();
               this.update(dt);
               this.drawFrame();
               frameRate = this.appScreen != GameView.AppScreen.PLAYING && !this.transitionVisible ? 30 : 60;
            }

            long target = 1000000000L / (long)frameRate;
            long remaining = target - (System.nanoTime() - frameStart);
            if (remaining > 0L) {
               try {
                  Thread.sleep(remaining / 1000000L, (int)(remaining % 1000000L));
               } catch (InterruptedException var20) {
                  Thread.currentThread().interrupt();
               }
            }
         }
      } finally {
         synchronized(this.renderThreadLock) {
            if (this.renderThread == currentThread) {
               this.renderThread = null;
            }
         }

         if (this.activityResumed && this.surfaceReady) {
            this.post(this::startThreadIfNeeded);
         }

      }

   }

   public boolean onTouchEvent(MotionEvent event) {
      int action = event.getActionMasked();
      if (action == 1) {
         this.performClick();
      }

      if (action != 0 && action != 5) {
         if (action == 2) {
            for(int i = 0; i < event.getPointerCount(); ++i) {
               this.touchCommandQueue.offer(2, event.getPointerId(i), event.getX(i), event.getY(i));
            }
         } else if (action != 1 && action != 6) {
            if (action == 3) {
               this.touchCommandQueue.offer(4, -1, 0.0F, 0.0F);
            }
         } else {
            int i = event.getActionIndex();
            this.touchCommandQueue.offer(3, event.getPointerId(i), event.getX(i), event.getY(i));
         }
      } else {
         int i = event.getActionIndex();
         this.touchCommandQueue.offer(1, event.getPointerId(i), event.getX(i), event.getY(i));
      }

      return true;
   }

   public boolean performClick() {
      super.performClick();
      return true;
   }

   public boolean handleBackPressed() {
      synchronized(this.gameStateLock) {
         if (this.transitionVisible) {
            this.transitionVisible = false;
            this.transitionStartingFromHome = false;
            this.pendingStartLevelIndex = -1;
            return true;
         } else if (this.mapConfirmVisible) {
            this.mapConfirmVisible = false;
            return true;
         } else if (this.appScreen == GameView.AppScreen.PLAYING) {
            if (this.state.isPlaying()) {
               this.state.pause();
               this.joystick.reset();
               this.boostPointerId = -1;
               this.resetExitPrompt();
               this.showMessage("行动已暂停，再按返回键退出关卡。", Color.rgb(104, 196, 202));
               return true;
            } else {
               this.showTitleScreen();
               return true;
            }
         } else if (this.appScreen != GameView.AppScreen.TITLE) {
            this.showTitleScreen();
            return true;
         } else {
            long now = System.currentTimeMillis();
            if (now - this.lastBackPressMillis > 1800L) {
               this.lastBackPressMillis = now;
               this.showMessage("再按一次返回键退出游戏。", Color.rgb(230, 236, 238));
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private void processTouchCommands() {
      while(this.touchCommandQueue.poll(this.touchCommand)) {
         this.handleTouchCommand(this.touchCommand);
      }

   }

   private void handleTouchCommand(TouchCommandQueue.Command c) {
      int type = c.getType();
      int id = c.getPointerId();
      float x = c.getX();
      float y = c.getY();
      if (type == 4) {
         this.joystick.reset();
         this.boostPointerId = -1;
      } else if (type == 2) {
         this.joystick.onPointerMove(id, x, y);
      } else if (type == 3) {
         if (this.boostPointerId == id) {
            this.boostPointerId = -1;
         }

         this.joystick.onPointerUp(id);
      } else if (type == 1 && !this.transitionVisible) {
         if (!this.mapConfirmVisible || this.appScreen != GameView.AppScreen.TITLE && this.appScreen != GameView.AppScreen.LEVEL_SELECT) {
            if (this.appScreen != GameView.AppScreen.PLAYING || !this.state.isPlaying() || !this.exitPromptVisible || !this.handleExitPromptTap(x, y)) {
               if (this.appScreen == GameView.AppScreen.PLAYING && this.state.isPlaying() && this.boostPointerId == -1 && this.boostButton.contains(x, y)) {
                  this.boostPointerId = id;
               } else {
                  if (this.appScreen == GameView.AppScreen.TITLE) {
                     this.handleTitleTap(x, y);
                  } else if (this.appScreen == GameView.AppScreen.CHARACTER_SELECT) {
                     this.handleCharacterSelectTap(x, y);
                  } else if (this.appScreen == GameView.AppScreen.LEVEL_SELECT) {
                     this.handleLevelSelectTap(x, y);
                  } else if (this.appScreen == GameView.AppScreen.SHOP) {
                     this.handleShopTap(x, y);
                  } else if (this.appScreen == GameView.AppScreen.SETTINGS) {
                     this.handleSettingsTap(x, y);
                  } else if (this.appScreen == GameView.AppScreen.PLAYING) {
                     boolean handled = this.handlePlayingTap(x, y);
                     if (!handled && this.state.isPlaying() && this.joystickTouchZone.contains(x, y)) {
                        this.joystick.onPointerDown(id, x, y);
                     }
                  }

               }
            }
         } else {
            this.handleMapConfirmTap(x, y);
         }
      }
   }

   private boolean handleTitleTap(float x, float y) {
      if (this.titleMapPrevButton.contains(x, y)) {
         this.feedback.click(this);
         this.shiftTitlePreviewLevel(-1);
         return true;
      } else if (this.titleMapNextButton.contains(x, y)) {
         this.feedback.click(this);
         this.shiftTitlePreviewLevel(1);
         return true;
      } else if (this.titleMapPreviewButton.contains(x, y)) {
         this.feedback.click(this);
         this.showMapConfirm(this.levelManager.getCurrentLevelNumber() - 1);
         return true;
      } else if (this.titleStartButton.contains(x, y)) {
         this.feedback.click(this);
         this.showMapConfirm(this.levelManager.getCurrentLevelNumber() - 1);
         return true;
      } else if (this.titleCharacterButton.contains(x, y)) {
         this.feedback.click(this);
         this.mapConfirmVisible = false;
         this.appScreen = GameView.AppScreen.CHARACTER_SELECT;
         this.configureViewport(this.screenWidth, this.screenHeight);
         return true;
      } else if (this.titleSelectButton.contains(x, y)) {
         this.feedback.click(this);
         this.mapConfirmVisible = false;
         this.appScreen = GameView.AppScreen.LEVEL_SELECT;
         this.configureViewport(this.screenWidth, this.screenHeight);
         return true;
      } else if (this.titleShopButton.contains(x, y)) {
         this.feedback.click(this);
         this.mapConfirmVisible = false;
         this.appScreen = GameView.AppScreen.SHOP;
         this.configureViewport(this.screenWidth, this.screenHeight);
         return true;
      } else if (this.titleSettingsButton.contains(x, y)) {
         this.feedback.click(this);
         this.mapConfirmVisible = false;
         this.appScreen = GameView.AppScreen.SETTINGS;
         this.configureViewport(this.screenWidth, this.screenHeight);
         return true;
      } else {
         return true;
      }
   }

   private boolean handleMapConfirmTap(float x, float y) {
      for(int i = 0; i < this.loadoutItemButtons.size() && i < SHOP_TYPES.length; ++i) {
         if (((RectF)this.loadoutItemButtons.get(i)).contains(x, y)) {
            this.feedback.click(this);
            this.toggleLoadoutType(SHOP_TYPES[i]);
            return true;
         }
      }

      if (this.mapConfirmCancelButton.contains(x, y)) {
         this.feedback.click(this);
         this.mapConfirmVisible = false;
         return true;
      } else if (this.mapConfirmStartButton.contains(x, y)) {
         this.feedback.click(this);
         this.mapConfirmVisible = false;
         this.beginHomeStartTransition(this.pendingStartLevelIndex);
         return true;
      } else {
         if (!this.mapConfirmPanel.contains(x, y)) {
            this.feedback.click(this);
            this.mapConfirmVisible = false;
         }

         return true;
      }
   }

   private boolean handleCharacterSelectTap(float x, float y) {
      if (this.characterBackButton.contains(x, y)) {
         this.feedback.click(this);
         this.showTitleScreen();
         return true;
      } else {
         List<CharacterConfig> characters = CharacterRepository.getAll();

         for(int i = 0; i < this.characterCardButtons.size() && i < characters.size(); ++i) {
            if (((RectF)this.characterCardButtons.get(i)).contains(x, y)) {
               this.selectedCharacter = (CharacterConfig)characters.get(i);
               this.selectedCharacterStore.saveSelectedCharacter(this.selectedCharacter);
               this.feedback.click(this);
               this.showTitleScreen();
               this.showMessage("已切换角色：" + this.selectedCharacter.getName(), this.selectedCharacter.getAccentColor());
               return true;
            }
         }

         return true;
      }
   }

   private boolean handleShopTap(float x, float y) {
      if (this.shopBackButton.contains(x, y)) {
         this.feedback.click(this);
         this.showTitleScreen();
         return true;
      } else {
         PowerUp.Type[] types = SHOP_TYPES;

         for(int i = 0; i < this.shopItemButtons.size() && i < types.length; ++i) {
            if (((RectF)this.shopItemButtons.get(i)).contains(x, y)) {
               this.feedback.click(this);
               this.buyShopItem(types[i]);
               return true;
            }
         }

         UpgradeType[] upgrades = UpgradeType.values();
         int offset = types.length;

         for(int i = 0; i < upgrades.length; ++i) {
            int bi = offset + i;
            if (bi < this.shopItemButtons.size() && ((RectF)this.shopItemButtons.get(bi)).contains(x, y)) {
               this.feedback.click(this);
               this.buyUpgrade(upgrades[i]);
               return true;
            }
         }

         return true;
      }
   }

   private boolean handleLevelSelectTap(float x, float y) {
      if (this.selectBackButton.contains(x, y)) {
         this.feedback.click(this);
         this.showTitleScreen();
         return true;
      } else {
         for(int i = 0; i < this.levelButtons.size(); ++i) {
            if (((RectF)this.levelButtons.get(i)).contains(x, y)) {
               this.feedback.click(this);
               this.showMapConfirm(i);
               return true;
            }
         }

         return true;
      }
   }

   private boolean handleSettingsTap(float x, float y) {
      if (this.settingsBackButton.contains(x, y)) {
         this.feedback.click(this);
         this.showTitleScreen();
         return true;
      } else if (this.routeToggleButton.contains(x, y)) {
         this.feedback.click(this);
         this.showPatrolRoutes = !this.showPatrolRoutes;
         this.saveSettings();
         return true;
      } else if (this.contrastToggleButton.contains(x, y)) {
         this.feedback.click(this);
         this.highContrastVision = !this.highContrastVision;
         this.saveSettings();
         return true;
      } else if (this.soundToggleButton.contains(x, y)) {
         this.feedback.click(this);
         this.soundFeedbackEnabled = !this.soundFeedbackEnabled;
         this.feedback.setSoundEnabled(this.soundFeedbackEnabled);
         this.saveSettings();
         return true;
      } else if (this.hapticToggleButton.contains(x, y)) {
         this.feedback.click(this);
         this.hapticFeedbackEnabled = !this.hapticFeedbackEnabled;
         this.feedback.setHapticEnabled(this.hapticFeedbackEnabled);
         this.saveSettings();
         return true;
      } else if (this.reduceMotionToggleButton.contains(x, y)) {
         this.feedback.click(this);
         this.reduceMotion = !this.reduceMotion;
         this.saveSettings();
         this.showMessage(this.reduceMotion ? "已减少界面动态效果。" : "已恢复完整动态效果。", Color.rgb(104, 196, 202));
         return true;
      } else if (this.largeTextToggleButton.contains(x, y)) {
         this.feedback.click(this);
         this.largeTextMode = !this.largeTextMode;
         this.saveSettings();
         this.showMessage(this.largeTextMode ? "大字体模式已开启。" : "大字体模式已关闭。", Color.rgb(104, 196, 202));
         return true;
      } else if (this.progressResetButton.contains(x, y)) {
         this.feedback.click(this);
         if (this.progressResetConfirmSeconds > 0.0F) {
            this.progressResetConfirmSeconds = 0.0F;
            this.resetProgress();
            this.showMessage("关卡星级进度已清空，金币、库存和升级保持不变。", Color.rgb(230, 236, 238));
         } else {
            this.progressResetConfirmSeconds = 4.0F;
            this.showMessage("再次点击“确认重置”才会清空全部关卡星级。", Color.rgb(235, 170, 92));
         }

         return true;
      } else {
         return true;
      }
   }

   private boolean handlePlayingTap(float x, float y) {
      if (this.state.isPlaying() && this.exitPromptVisible && this.handleExitPromptTap(x, y)) {
         return true;
      } else if (this.homeButton.contains(x, y)) {
         this.feedback.click(this);
         this.showTitleScreen();
         return true;
      } else if (this.restartButton.contains(x, y)) {
         this.feedback.click(this);
         this.resetLevel();
         this.showMessage("路线重置，重新观察安保节奏。", Color.rgb(230, 236, 238));
         return true;
      } else if (this.pauseButton.contains(x, y)) {
         this.feedback.click(this);
         this.state.togglePause();
         this.resetExitPrompt();
         this.joystick.reset();
         this.boostPointerId = -1;
         this.showMessage(this.state.isPaused() ? "已暂停。" : "继续潜行。", Color.rgb(230, 236, 238));
         return true;
      } else if (this.state.isPaused()) {
         if (this.overlayActionButton.contains(x, y)) {
            this.feedback.click(this);
            this.state.resumePlaying();
            this.showMessage("继续潜行。", Color.rgb(230, 236, 238));
         }

         return true;
      } else if (!this.state.isCleared()) {
         if (this.state.isFailed()) {
            this.feedback.click(this);
            this.resetLevel();
            this.showMessage("路线重置，换一条更安静的路径。", Color.rgb(230, 236, 238));
            return true;
         } else {
            for(int i = 0; i < this.powerUpButtons.size(); ++i) {
               if (((RectF)this.powerUpButtons.get(i)).contains(x, y)) {
                  this.feedback.click(this);
                  this.useStoredPowerUp(i);
                  return true;
               }
            }

            for(int i = 0; i < this.hotbarSlots.size(); ++i) {
               if (((RectF)this.hotbarSlots.get(i)).contains(x, y)) {
                  this.feedback.click(this);
                  this.dropHotbarSlot(i);
                  return true;
               }
            }

            Door touchedDoor = this.findTouchedDoor(x, y);
            if (touchedDoor != null) {
               this.feedback.click(this);
               this.toggleDoor(touchedDoor);
               return true;
            } else {
               return !this.state.isPlaying();
            }
         }
      } else {
         if (this.overlayActionButton.contains(x, y) || this.nextButton.contains(x, y)) {
            this.feedback.click(this);
            this.advanceAfterClear();
         }

         return true;
      }
   }

   private boolean handleExitPromptTap(float x, float y) {
      if (this.exitPromptVisible && !this.exitPromptNeedsTreasure) {
         if (this.exitConfirmButton.contains(x, y)) {
            this.feedback.click(this);
            this.completeLevel();
            return true;
         } else if (this.exitContinueButton.contains(x, y)) {
            this.feedback.click(this);
            this.suppressExitPromptUntilLeave();
            this.showMessage("继续盗取，离开撤离区后倒计时重置。", Color.rgb(104, 196, 202));
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void startThreadIfNeeded() {
      synchronized(this.renderThreadLock) {
         if (this.activityResumed && this.surfaceReady && this.holder.getSurface().isValid()) {
            if (this.renderThread == null || !this.renderThread.isAlive()) {
               this.running = true;
               this.renderThread = new Thread(this, "MuseumHeistGameLoop");
               this.renderThread.start();
            }
         }
      }
   }

   private boolean shouldRender(Thread thread) {
      return this.running && this.activityResumed && this.surfaceReady && this.holder.getSurface().isValid() && this.renderThread == thread && !thread.isInterrupted();
   }

   private void stopRenderThread() {
      Thread thread;
      synchronized(this.renderThreadLock) {
         this.running = false;
         thread = this.renderThread;
         if (thread != null) {
            thread.interrupt();
         }
      }

      if (thread != null && thread != Thread.currentThread()) {
         try {
            thread.join(180L);
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
         }
      }

      synchronized(this.renderThreadLock) {
         if (this.renderThread == thread && (thread == null || !thread.isAlive())) {
            this.renderThread = null;
         }

      }
   }

   private void configureViewport(int width, int height) {
      int safeWidth = Math.max(width, 1);
      int safeHeight = Math.max(height, 1);
      this.screenWidth = safeWidth;
      this.screenHeight = safeHeight;
      this.uiScale = this.clamp(Math.min((float)safeWidth / 1280.0F, (float)safeHeight / 720.0F), 0.74F, 1.28F);
      this.worldScale = this.clamp(Math.min((float)safeWidth / 980.0F, (float)safeHeight / 560.0F), 0.86F, 1.34F);
      float hudWidth = Math.min(600.0F * this.uiScale, (float)safeWidth * 0.54F);
      float hudHeight = 96.0F * this.uiScale;
      this.hudPanel.set(14.0F * this.uiScale, 12.0F * this.uiScale, 14.0F * this.uiScale + hudWidth, 12.0F * this.uiScale + hudHeight);
      this.timerPanel.set(this.hudPanel.left, this.hudPanel.bottom + 8.0F * this.uiScale, this.hudPanel.left + Math.min(230.0F * this.uiScale, this.hudPanel.width() * 0.46F), this.hudPanel.bottom + 42.0F * this.uiScale);
      float joystickRadius = this.clamp((float)Math.max(safeWidth, safeHeight) * 0.09F, 66.0F * this.uiScale, 112.0F * this.uiScale);
      this.joystick.setMaxDistance(joystickRadius);
      this.joystick.setDefaultPosition(Math.max(102.0F * this.uiScale, (float)safeWidth * 0.105F), (float)safeHeight - 118.0F * this.uiScale);
      this.joystickTouchZone.set(0.0F, (float)safeHeight * 0.3F, (float)safeWidth * 0.44F, (float)safeHeight);
      this.configureMiniMap(safeWidth, safeHeight);
      float actionSize = this.clamp(72.0F * this.uiScale, 64.0F, 92.0F);
      float margin = 20.0F * this.uiScale;
      float gap = 13.0F * this.uiScale;
      float totalActionsWidth = actionSize * 3.0F + gap * 2.0F;
      float actionLeft = this.miniMapPanel.right - totalActionsWidth;
      float actionsTop = this.miniMapPanel.bottom + 12.0F * this.uiScale;
      this.homeButton.set(actionLeft, actionsTop, actionLeft + actionSize, actionsTop + actionSize);
      this.pauseButton.set(this.homeButton.right + gap, actionsTop, this.homeButton.right + gap + actionSize, actionsTop + actionSize);
      this.restartButton.set(this.pauseButton.right + gap, actionsTop, this.pauseButton.right + gap + actionSize, actionsTop + actionSize);
      float skillSize = this.clamp(166.0F * this.uiScale, 142.0F, 198.0F);
      float itemSize = this.clamp(134.0F * this.uiScale, 118.0F, 160.0F);
      float actionGap = gap + 12.0F * this.uiScale;
      float skillLeft = (float)safeWidth - margin - skillSize - 10.0F * this.uiScale;
      float skillBottom = (float)safeHeight - 42.0F * this.uiScale;
      this.boostButton.set(skillLeft, skillBottom - skillSize, skillLeft + skillSize, skillBottom);
      float itemLeft = this.boostButton.left - actionGap - itemSize;
      float itemTop = this.boostButton.top + skillSize * 0.16F;
      this.itemButton.set(Math.max((float)safeWidth * 0.5F, itemLeft), itemTop, Math.max((float)safeWidth * 0.5F, itemLeft) + itemSize, itemTop + itemSize);
      this.configurePowerUpButtons(this.itemButton, itemSize);
      this.configureHotbar(safeWidth, safeHeight);
      this.configureExitPrompt(safeWidth, safeHeight);
      float titleButtonWidth = Math.min(510.0F * this.uiScale, (float)safeWidth * 0.355F);
      float titleButtonHeight = this.clamp((float)safeHeight * 0.088F, 50.0F * this.uiScale, 68.0F * this.uiScale);
      float titleButtonCenterX = (float)safeWidth * 0.75F;
      float titlePanelTop = (float)safeHeight * 0.06F;
      float titlePanelBottom = (float)safeHeight - 24.0F * this.uiScale;
      float titleTop = Math.max((float)safeHeight * 0.235F, titlePanelTop + 124.0F * this.uiScale);
      float titleGap = 8.0F * this.uiScale;
      this.titleStartButton.set(titleButtonCenterX - titleButtonWidth * 0.5F, titleTop, titleButtonCenterX + titleButtonWidth * 0.5F, titleTop + titleButtonHeight);
      this.titleCharacterButton.set(titleButtonCenterX - titleButtonWidth * 0.5F, this.titleStartButton.bottom + titleGap, titleButtonCenterX + titleButtonWidth * 0.5F, this.titleStartButton.bottom + titleGap + titleButtonHeight);
      this.titleSelectButton.set(titleButtonCenterX - titleButtonWidth * 0.5F, this.titleCharacterButton.bottom + titleGap, titleButtonCenterX + titleButtonWidth * 0.5F, this.titleCharacterButton.bottom + titleGap + titleButtonHeight);
      this.titleShopButton.set(titleButtonCenterX - titleButtonWidth * 0.5F, this.titleSelectButton.bottom + titleGap, titleButtonCenterX + titleButtonWidth * 0.5F, this.titleSelectButton.bottom + titleGap + titleButtonHeight);
      this.titleSettingsButton.set(titleButtonCenterX - titleButtonWidth * 0.5F, this.titleShopButton.bottom + titleGap, titleButtonCenterX + titleButtonWidth * 0.5F, this.titleShopButton.bottom + titleGap + titleButtonHeight);
      float progressTop = this.titleSettingsButton.bottom + 10.0F * this.uiScale;
      float progressHeight = 60.0F * this.uiScale;
      float footerHeight = 38.0F * this.uiScale;
      float footerTop = titlePanelBottom - footerHeight;
      float progressBottomLimit = footerTop - 18.0F * this.uiScale;
      this.titleProgressPanel.set(this.titleStartButton.left, progressTop, this.titleStartButton.right, Math.max(progressTop + 42.0F * this.uiScale, Math.min(progressTop + progressHeight, progressBottomLimit)));
      this.titleFooterPanel.set(this.titleStartButton.left, footerTop, this.titleStartButton.right, titlePanelBottom);
      this.selectBackButton.set(20.0F * this.uiScale, 18.0F * this.uiScale, 132.0F * this.uiScale, 62.0F * this.uiScale);
      this.characterBackButton.set(20.0F * this.uiScale, 18.0F * this.uiScale, 132.0F * this.uiScale, 62.0F * this.uiScale);
      this.settingsBackButton.set(20.0F * this.uiScale, 18.0F * this.uiScale, 132.0F * this.uiScale, 62.0F * this.uiScale);
      this.shopBackButton.set(20.0F * this.uiScale, 18.0F * this.uiScale, 132.0F * this.uiScale, 62.0F * this.uiScale);
      float settingsWidth = Math.min(620.0F * this.uiScale, (float)safeWidth * 0.72F);
      float settingsLeft = (float)safeWidth * 0.5F - settingsWidth * 0.5F;
      float settingsTop = (float)safeHeight * 0.225F;
      float settingsRowHeight = this.clamp((float)safeHeight * 0.063F, 40.0F * this.uiScale, 50.0F * this.uiScale);
      float settingsRowGap = 7.0F * this.uiScale;
      this.routeToggleButton.set(settingsLeft, settingsTop, settingsLeft + settingsWidth, settingsTop + settingsRowHeight);
      this.contrastToggleButton.set(settingsLeft, this.routeToggleButton.bottom + settingsRowGap, settingsLeft + settingsWidth, this.routeToggleButton.bottom + settingsRowGap + settingsRowHeight);
      this.soundToggleButton.set(settingsLeft, this.contrastToggleButton.bottom + settingsRowGap, settingsLeft + settingsWidth, this.contrastToggleButton.bottom + settingsRowGap + settingsRowHeight);
      this.hapticToggleButton.set(settingsLeft, this.soundToggleButton.bottom + settingsRowGap, settingsLeft + settingsWidth, this.soundToggleButton.bottom + settingsRowGap + settingsRowHeight);
      this.reduceMotionToggleButton.set(settingsLeft, this.hapticToggleButton.bottom + settingsRowGap, settingsLeft + settingsWidth, this.hapticToggleButton.bottom + settingsRowGap + settingsRowHeight);
      this.largeTextToggleButton.set(settingsLeft, this.reduceMotionToggleButton.bottom + settingsRowGap, settingsLeft + settingsWidth, this.reduceMotionToggleButton.bottom + settingsRowGap + settingsRowHeight);
      this.progressResetButton.set(settingsLeft, this.largeTextToggleButton.bottom + settingsRowGap, settingsLeft + settingsWidth, this.largeTextToggleButton.bottom + settingsRowGap + settingsRowHeight);
      this.overlayActionButton.set((float)safeWidth * 0.5F - 190.0F * this.uiScale, (float)safeHeight * 0.64F, (float)safeWidth * 0.5F + 190.0F * this.uiScale, (float)safeHeight * 0.64F + 76.0F * this.uiScale);
      this.nextButton.set(this.overlayActionButton);
      this.configureMapConfirm(safeWidth, safeHeight);
      this.configureLevelButtons(safeWidth, safeHeight);
      this.configureCharacterCards(safeWidth, safeHeight);
      this.configureShopButtons(safeWidth, safeHeight);
      this.validateLayout(safeWidth, safeHeight);
   }

   private void configureMapConfirm(int safeWidth, int safeHeight) {
      float panelWidth = Math.min(760.0F * this.uiScale, (float)safeWidth * 0.68F);
      float panelHeight = Math.min(430.0F * this.uiScale, (float)safeHeight * 0.74F);
      this.mapConfirmPanel.set((float)safeWidth * 0.5F - panelWidth * 0.5F, (float)safeHeight * 0.5F - panelHeight * 0.5F, (float)safeWidth * 0.5F + panelWidth * 0.5F, (float)safeHeight * 0.5F + panelHeight * 0.5F);
      this.loadoutItemButtons.clear();
      float gap = 8.0F * this.uiScale;
      float left = this.mapConfirmPanel.left + 24.0F * this.uiScale;
      float top = this.mapConfirmPanel.top + 198.0F * this.uiScale;
      float width = (this.mapConfirmPanel.width() - 48.0F * this.uiScale - gap * (float)(SHOP_TYPES.length - 1)) / (float)SHOP_TYPES.length;
      float height = 94.0F * this.uiScale;

      for(int i = 0; i < SHOP_TYPES.length; ++i) {
         float x = left + (float)i * (width + gap);
         this.loadoutItemButtons.add(new RectF(x, top, x + width, top + height));
      }

      float buttonGap = 14.0F * this.uiScale;
      float buttonHeight = 56.0F * this.uiScale;
      float buttonWidth = (this.mapConfirmPanel.width() - 44.0F * this.uiScale - buttonGap) * 0.5F;
      float buttonTop = this.mapConfirmPanel.bottom - buttonHeight - 22.0F * this.uiScale;
      this.mapConfirmCancelButton.set(this.mapConfirmPanel.left + 22.0F * this.uiScale, buttonTop, this.mapConfirmPanel.left + 22.0F * this.uiScale + buttonWidth, buttonTop + buttonHeight);
      this.mapConfirmStartButton.set(this.mapConfirmCancelButton.right + buttonGap, buttonTop, this.mapConfirmCancelButton.right + buttonGap + buttonWidth, buttonTop + buttonHeight);
   }

   private void configurePowerUpButtons(RectF anchor, float itemSize) {
      this.powerUpButtons.clear();

      for(int i = 0; i < 2; ++i) {
         float offset = (float)i * (itemSize + 10.0F * this.uiScale);
         this.powerUpButtons.add(new RectF(anchor.left - offset, anchor.top, anchor.right - offset, anchor.bottom));
      }

      if (!this.powerUpButtons.isEmpty()) {
         this.itemButton.set((RectF)this.powerUpButtons.get(0));
      }

   }

   private void configureHotbar(int safeWidth, int safeHeight) {
      this.hotbarSlots.clear();
      float slot = this.clamp(52.0F * this.uiScale, 44.0F, 64.0F);
      float gap = 7.0F * this.uiScale;
      float totalWidth = 5.0F * slot + 4.0F * gap;
      float left = (float)safeWidth * 0.5F - totalWidth * 0.5F - 24.0F * this.uiScale;
      if (!this.powerUpButtons.isEmpty()) {
         RectF leftmostPowerUp = (RectF)this.powerUpButtons.get(this.powerUpButtons.size() - 1);
         left = Math.min(left, leftmostPowerUp.left - totalWidth - 20.0F * this.uiScale);
      }

      float top = (float)safeHeight - slot - 20.0F * this.uiScale;
      this.inventoryPanel.set(left - 10.0F * this.uiScale, top - 10.0F * this.uiScale, left + totalWidth + 10.0F * this.uiScale, top + slot + 10.0F * this.uiScale);

      for(int i = 0; i < 5; ++i) {
         float x = left + (float)i * (slot + gap);
         this.hotbarSlots.add(new RectF(x, top, x + slot, top + slot));
      }

   }

   private void configureExitPrompt(int safeWidth, int safeHeight) {
      float panelWidth = Math.min(560.0F * this.uiScale, (float)safeWidth * 0.54F);
      float panelHeight = 178.0F * this.uiScale;
      float bottomClearance = Math.max(126.0F * this.uiScale, (float)safeHeight * 0.18F);
      float left = (float)safeWidth * 0.5F - panelWidth * 0.5F;
      float top = (float)safeHeight - bottomClearance - panelHeight;
      top = this.clamp(top, (float)safeHeight * 0.42F, (float)safeHeight - panelHeight - 96.0F * this.uiScale);
      this.exitPromptPanel.set(left, top, left + panelWidth, top + panelHeight);
      float buttonGap = 12.0F * this.uiScale;
      float buttonHeight = 46.0F * this.uiScale;
      float buttonWidth = (panelWidth - 42.0F * this.uiScale - buttonGap) * 0.5F;
      float buttonTop = this.exitPromptPanel.bottom - buttonHeight - 18.0F * this.uiScale;
      this.exitContinueButton.set(this.exitPromptPanel.left + 18.0F * this.uiScale, buttonTop, this.exitPromptPanel.left + 18.0F * this.uiScale + buttonWidth, buttonTop + buttonHeight);
      this.exitConfirmButton.set(this.exitContinueButton.right + buttonGap, buttonTop, this.exitContinueButton.right + buttonGap + buttonWidth, buttonTop + buttonHeight);
   }

   private void configureLevelButtons(int safeWidth, int safeHeight) {
      this.levelButtons.clear();
      int count = this.levelManager.getLevelCount();
      int columns = safeWidth >= 900 ? 5 : (safeWidth >= 720 ? 4 : 3);
      int rows = Math.max(1, (int)Math.ceil((double)((float)count / (float)columns)));
      float gridWidth = Math.min(1320.0F * this.uiScale, (float)safeWidth * 0.94F);
      float gridHeight = Math.min(486.0F * this.uiScale, (float)safeHeight * 0.66F);
      float cellWidth = gridWidth / (float)columns;
      float cellHeight = gridHeight / (float)rows;
      float gridLeft = (float)safeWidth * 0.5F - gridWidth * 0.5F;
      float gridTop = (float)safeHeight * 0.275F;

      for(int i = 0; i < count; ++i) {
         int row = i / columns;
         int column = i % columns;
         float left = gridLeft + (float)column * cellWidth + 7.0F * this.uiScale;
         float top = gridTop + (float)row * cellHeight + 7.0F * this.uiScale;
         this.levelButtons.add(new RectF(left, top, left + cellWidth - 14.0F * this.uiScale, top + cellHeight - 14.0F * this.uiScale));
      }

   }

   private void configureCharacterCards(int safeWidth, int safeHeight) {
      this.characterCardButtons.clear();
      int count = CharacterRepository.getAll().size();
      int columns = safeWidth >= 900 ? 5 : (safeWidth >= 680 ? 3 : 2);
      int rows = Math.max(1, (int)Math.ceil((double)((float)count / (float)columns)));
      float gridWidth = Math.min(1240.0F * this.uiScale, (float)safeWidth * 0.94F);
      float gridHeight = Math.min((float)rows * 560.0F * this.uiScale, (float)safeHeight * 0.70F);
      float cellWidth = gridWidth / (float)columns;
      float cellHeight = gridHeight / (float)rows;
      float left = (float)safeWidth * 0.5F - gridWidth * 0.5F;
      float top = (float)safeHeight * 0.255F;

      for(int i = 0; i < count; ++i) {
         int row = i / columns;
         int column = i % columns;
         float cardLeft = left + (float)column * cellWidth + 7.0F * this.uiScale;
         float cardTop = top + (float)row * cellHeight + 7.0F * this.uiScale;
         this.characterCardButtons.add(new RectF(cardLeft, cardTop, cardLeft + cellWidth - 14.0F * this.uiScale, cardTop + cellHeight - 14.0F * this.uiScale));
      }

   }

   private void configureShopButtons(int safeWidth, int safeHeight) {
      this.shopItemButtons.clear();
      int itemCount = this.shopTypes().length + UpgradeType.values().length;
      int columns = safeWidth >= 680 ? 3 : 2;
      int rows = Math.max(1, (int)Math.ceil((double)((float)itemCount / (float)columns)));
      float gridWidth = Math.min(1240.0F * this.uiScale, (float)safeWidth * 0.94F);
      float cellWidth = gridWidth / (float)columns;
      float gridTop = (float)safeHeight * 0.235F;
      float availableHeight = (float)safeHeight - gridTop - 18.0F * this.uiScale;
      float cellHeight = Math.min(152.0F * this.uiScale, availableHeight / (float)rows);
      float gridLeft = (float)safeWidth * 0.5F - gridWidth * 0.5F;

      for(int i = 0; i < itemCount; ++i) {
         int row = i / columns;
         int column = i % columns;
         float left = gridLeft + (float)column * cellWidth + 8.0F * this.uiScale;
         float top = gridTop + (float)row * cellHeight + 8.0F * this.uiScale;
         this.shopItemButtons.add(new RectF(left, top, left + cellWidth - 16.0F * this.uiScale, top + cellHeight - 16.0F * this.uiScale));
      }

   }

   private void configureMiniMap(int safeWidth, int safeHeight) {
      Level level = this.getLevel();
      float miniWidth = this.clamp((float)safeWidth * 0.23F, 190.0F * this.uiScale, 302.0F * this.uiScale);
      float miniHeight = miniWidth * level.getWorldHeight() / Math.max(level.getWorldWidth(), 1.0F);
      miniHeight = this.clamp(miniHeight, 116.0F * this.uiScale, 204.0F * this.uiScale);
      float right = (float)safeWidth - 18.0F * this.uiScale;
      float top = 14.0F * this.uiScale;
      this.miniMapPanel.set(right - miniWidth, top, right, top + miniHeight);
   }

   private void validateLayout(int safeWidth, int safeHeight) {
      RectF screen = new RectF(0.0F, 0.0F, (float)safeWidth, (float)safeHeight);
      RectF[] titleActions = new RectF[]{this.titleStartButton, this.titleCharacterButton, this.titleSelectButton, this.titleShopButton, this.titleSettingsButton, this.titleProgressPanel, this.titleFooterPanel};
      this.warnGroupOverlap("title-column", titleActions);

      for(int i = 0; i < titleActions.length; ++i) {
         this.warnOutside("title-region-" + i, titleActions[i], screen);
      }

      this.warnOverlap("mini-map", this.miniMapPanel, "home", this.homeButton);
      this.warnOverlap("mini-map", this.miniMapPanel, "pause", this.pauseButton);
      this.warnOverlap("mini-map", this.miniMapPanel, "restart", this.restartButton);
      this.warnOutside("mini-map", this.miniMapPanel, screen);
      this.warnOutside("home", this.homeButton, screen);
      this.warnOutside("pause", this.pauseButton, screen);
      this.warnOutside("restart", this.restartButton, screen);
      this.warnOutside("boost", this.boostButton, screen);
      this.warnOutside("hotbar", this.inventoryPanel, screen);
      this.warnOverlap("boost", this.boostButton, "hotbar", this.inventoryPanel);

      for(int i = 0; i < this.powerUpButtons.size(); ++i) {
         RectF powerUp = (RectF)this.powerUpButtons.get(i);
         this.warnOverlap("power-up-" + i, powerUp, "boost", this.boostButton);
         this.warnOverlap("power-up-" + i, powerUp, "hotbar", this.inventoryPanel);
         this.warnOutside("power-up-" + i, powerUp, screen);
      }

      RectF[] settingsControls = new RectF[]{this.routeToggleButton, this.contrastToggleButton, this.soundToggleButton, this.hapticToggleButton, this.reduceMotionToggleButton, this.largeTextToggleButton, this.progressResetButton};
      this.warnGroupOverlap("settings-control", settingsControls);

      for(int i = 0; i < settingsControls.length; ++i) {
         this.warnOutside("settings-control-" + i, settingsControls[i], screen);
      }

      this.warnListOverlap("character-card", this.characterCardButtons);
      this.warnListOverlap("level-card", this.levelButtons);
      this.warnListOverlap("shop-card", this.shopItemButtons);
      this.warnListOutside("character-card", this.characterCardButtons, screen);
      this.warnListOutside("level-card", this.levelButtons, screen);
      this.warnListOutside("shop-card", this.shopItemButtons, screen);
   }

   private void warnGroupOverlap(String group, RectF[] bounds) {
      for(int i = 0; i < bounds.length; ++i) {
         for(int j = i + 1; j < bounds.length; ++j) {
            this.warnOverlap(group + "-" + i, bounds[i], group + "-" + j, bounds[j]);
         }
      }

   }

   private void warnListOverlap(String group, List bounds) {
      for(int i = 0; i < bounds.size(); ++i) {
         for(int j = i + 1; j < bounds.size(); ++j) {
            this.warnOverlap(group + "-" + i, (RectF)bounds.get(i), group + "-" + j, (RectF)bounds.get(j));
         }
      }

   }

   private void warnListOutside(String group, List bounds, RectF screen) {
      for(int i = 0; i < bounds.size(); ++i) {
         this.warnOutside(group + "-" + i, (RectF)bounds.get(i), screen);
      }

   }

   private void warnOverlap(String firstName, RectF first, String secondName, RectF second) {
      if (!first.isEmpty() && !second.isEmpty() && RectF.intersects(first, second)) {
         Log.w("MuseumHeistGameView", "Layout overlap: " + firstName + " " + first + " / " + secondName + " " + second);
      }

   }

   private void warnOutside(String name, RectF bounds, RectF screen) {
      if (!bounds.isEmpty() && !screen.contains(bounds)) {
         Log.w("MuseumHeistGameView", "Layout outside screen: " + name + " " + bounds + " / " + screen);
      }

   }

   private void loadProgress() {
      this.progressStore.loadBestStars(this.bestStars);
      this.coinsBalance = this.progressStore.loadCoins();
      this.progressStore.loadShopStock(this.shopStock);
      this.progressStore.loadUpgradeLevels(this.upgradeLevels);
      PowerUp.Type[] saved = new PowerUp.Type[2];
      this.progressStore.loadLoadout(saved);
      this.loadoutSelection.restore(saved);
      this.showPatrolRoutes = this.progressStore.loadShowPatrolRoutes();
      this.highContrastVision = this.progressStore.loadHighContrastVision();
      this.soundFeedbackEnabled = this.progressStore.loadSoundFeedbackEnabled();
      this.hapticFeedbackEnabled = this.progressStore.loadHapticFeedbackEnabled();
      this.reduceMotion = this.progressStore.loadReduceMotion();
      this.largeTextMode = this.progressStore.loadLargeText();
      this.feedback.setSoundEnabled(this.soundFeedbackEnabled);
      this.feedback.setHapticEnabled(this.hapticFeedbackEnabled);
      this.selectedCharacter = this.selectedCharacterStore.loadSelectedCharacter();
   }

   private void saveSettings() {
      this.progressStore.saveSettings(this.showPatrolRoutes, this.highContrastVision, this.soundFeedbackEnabled, this.hapticFeedbackEnabled, this.reduceMotion, this.largeTextMode);
   }

   private void saveLoadout() {
      PowerUp.Type[] a = new PowerUp.Type[2];
      this.loadoutSelection.copyTo(a);
      this.progressStore.saveLoadout(a);
   }

   private void toggleLoadoutType(PowerUp.Type type) {
      if (this.shopStock[type.ordinal()] <= 0 && !this.loadoutSelection.contains(type)) {
         this.showMessage(type.getLabel() + "库存不足，请先前往装备商店。", Color.rgb(235, 170, 92));
      } else {
         LoadoutSelection.ToggleResult result = this.loadoutSelection.toggle(type);
         if (result == ToggleResult.FULL) {
            this.showMessage("最多只能携带两件预备道具。", Color.rgb(235, 170, 92));
         } else {
            this.saveLoadout();
            this.showMessage(result == ToggleResult.SELECTED ? "已装备：" + type.getLabel() : "已卸下：" + type.getLabel(), this.powerUpColor(type));
         }
      }
   }

   private void saveLevelProgress(int levelIndex, int stars) {
      if (levelIndex >= 0 && levelIndex < this.bestStars.length) {
         int safeStars = Math.max(0, Math.min(3, stars));
         if (safeStars > this.bestStars[levelIndex]) {
            this.bestStars[levelIndex] = safeStars;
            this.progressStore.saveLevelStars(levelIndex, safeStars);
         }
      }
   }

   private void resetProgress() {
      this.progressStore.resetLevelStars(this.bestStars);
   }

   private void saveCoins() {
      this.progressStore.saveCoins(this.coinsBalance);
   }

   private void saveShopStock(PowerUp.Type type) {
      this.progressStore.saveShopStock(type, this.shopStock[type.ordinal()]);
   }

   private void saveUpgradeLevel(UpgradeType type) {
      this.progressStore.saveUpgradeLevel(type, this.upgradeLevels[type.ordinal()]);
   }

   private void addCoins(int amount) {
      if (amount > 0) {
         this.coinsBalance += amount;
         this.saveCoins();
      }
   }

   private void addRunCoins(int amount) {
      this.runCoinTotal += Math.max(0, amount);
   }

   private void buyShopItem(PowerUp.Type type) {
      int index = type.ordinal();
      int price = this.shopPrice(type);
      if (this.shopStock[index] >= 99) {
         this.showMessage(type.getLabel() + "库存已达 99 件", Color.rgb(220, 226, 228));
      } else if (this.coinsBalance < price) {
         this.showMessage("行动资金不足", Color.rgb(235, 88, 80));
      } else {
         this.coinsBalance -= price;
         int var10002 = this.shopStock[index]++;
         this.progressStore.commitPowerUpPurchase(type, this.coinsBalance, this.shopStock[index]);
         if (this.loadoutSelection.selectIfSpace(type)) {
            this.saveLoadout();
         }

         this.showMessage("已购买：" + type.getLabel() + " · 库存 " + this.shopStock[index], this.powerUpColor(type));
      }
   }

   private void buyUpgrade(UpgradeType type) {
      int currentLevel = this.upgradeLevels[type.ordinal()];
      if (currentLevel >= 3) {
         this.showMessage(type.getLabel() + "已升满。", Color.rgb(220, 226, 228));
      } else {
         int price = this.upgradePrice(type);
         if (this.coinsBalance < price) {
            this.showMessage("金币不足。", Color.rgb(235, 88, 80));
         } else {
            this.coinsBalance -= price;
            int var10002 = this.upgradeLevels[type.ordinal()]++;
            this.progressStore.commitUpgradePurchase(type, this.coinsBalance, this.upgradeLevels[type.ordinal()]);
            this.showMessage("永久升级：" + type.getLabel() + " Lv." + this.upgradeLevels[type.ordinal()], Color.rgb(213, 168, 79));
         }
      }
   }

   private void loadPurchasedPowerUps() {
      for(int slot = 0; slot < 2; ++slot) {
         PowerUp.Type type = this.loadoutSelection.get(slot);
         if (type != null && this.state.canStorePowerUp()) {
            int index = type.ordinal();
            if (this.shopStock[index] > 0) {
               this.state.storePowerUp(type);
               int var10002 = this.shopStock[index]--;
               var10002 = this.runStashLoaded[index]++;
            }
         }
      }

   }

   private void consumeRunStashItem(PowerUp.Type type) {
      if (type != null) {
         int index = type.ordinal();
         if (index >= 0 && index < this.runStashLoaded.length && this.runStashLoaded[index] > 0) {
            int var10002 = this.runStashLoaded[index]--;
            this.persistCommittedShopStock();
         }

      }
   }

   private void persistCommittedShopStock() {
      int[] committedStock = (int[])this.shopStock.clone();

      for(PowerUp.Type type : Type.values()) {
         int index = type.ordinal();
         committedStock[index] = Math.min(99, committedStock[index] + this.runStashLoaded[index]);
      }

      this.progressStore.saveShopStock(committedStock);
   }

   private void refundUnusedRunStash() {
      boolean changed = false;

      for(PowerUp.Type type : Type.values()) {
         int index = type.ordinal();
         if (this.runStashLoaded[index] > 0) {
            this.shopStock[index] = Math.min(99, this.shopStock[index] + this.runStashLoaded[index]);
            this.runStashLoaded[index] = 0;
            changed = true;
         }
      }

      if (changed) {
         this.progressStore.saveShopStock(this.shopStock);
      }

   }

   private void resetLevel() {
      this.resetLevel(true);
   }

   private void resetLevel(boolean loadPurchasedItems) {
      this.refundUnusedRunStash();
      Level level = this.getLevel();
      this.state.reset(level.getTreasureCount());
      level.resetDynamicObjects();
      this.player.reset(level.getPlayerStartX(), level.getPlayerStartY());
      this.joystick.reset();
      this.boostPointerId = -1;
      this.resetExitPrompt();
      this.exitPromptSuppressedUntilLeave = false;
      this.powerUps.clear();
      this.coins.clear();
      this.disruptors.clear();
      this.floatingText.clear();
      this.levelResult.reset();
      this.stealthTracker.reset();
      this.runCoinTotal = 0;
      this.nextPowerUpSeconds = 2.5F;
      this.treasureClaimSeconds = 0.0F;
      this.treasureClaimIndex = -1;
      this.decoySeconds = 0.0F;
      this.levelElapsedSeconds = 0.0F;
      if (loadPurchasedItems) {
         this.loadPurchasedPowerUps();
      }

      this.spawnCoinsForLevel(level);
      this.spawnPowerUpsToTarget();
      this.updateCamera(0.0F, true);
   }

   private void showTitleScreen() {
      this.levelManager.restartCampaign();
      this.appScreen = GameView.AppScreen.TITLE;
      this.mapConfirmVisible = false;
      this.transitionVisible = false;
      this.resetLevel(false);
      this.configureViewport(this.screenWidth, this.screenHeight);
   }

   private void startCampaign() {
      this.levelManager.restartCampaign();
      this.startCurrentLevel();
   }

   private void startLevel(int index) {
      this.levelManager.setCurrentLevel(this.clampLevelIndex(index));
      this.startCurrentLevel();
   }

   private void startCurrentLevel() {
      this.transitionVisible = false;
      this.transitionStartingFromHome = false;
      this.transitionAdvanceLevel = false;
      this.transitionRestartCampaign = false;
      this.appScreen = GameView.AppScreen.PLAYING;
      this.configureViewport(this.getWidth(), this.getHeight());
      this.resetLevel();
      this.showMessage("观察视锥和巡逻路线，等待空隙再行动。", Color.rgb(230, 236, 238));
   }

   private void advanceAfterClear() {
      if (!this.transitionVisible) {
         this.transitionVisible = true;
         this.transitionSeconds = 0.0F;
         this.transitionStartingFromHome = false;
         this.transitionAdvanceLevel = this.levelManager.hasNextLevel();
         this.transitionRestartCampaign = !this.transitionAdvanceLevel;
         this.transitionTitle = this.transitionAdvanceLevel ? "进入下一展厅" : "夜巡路线重置";
         this.transitionSubtitle = this.transitionAdvanceLevel ? "正在重新规划巡逻节奏与展柜路线。" : "全部路线已完成，准备从前厅重新开始。";
      }
   }

   private void finishAdvanceAfterClear() {
      if (this.levelManager.hasNextLevel()) {
         this.levelManager.goToNextLevel();
         this.showMessage("已进入下一间展厅。", Color.rgb(213, 168, 79));
      } else {
         this.levelManager.restartCampaign();
         this.showMessage("全馆路线重置，新的夜巡开始。", Color.rgb(213, 168, 79));
      }

      this.configureViewport(this.getWidth(), this.getHeight());
      this.resetLevel();
   }

   private void showMapConfirm(int levelIndex) {
      this.pendingStartLevelIndex = this.clampLevelIndex(levelIndex);
      this.mapConfirmVisible = true;
   }

   private void beginHomeStartTransition(int levelIndex) {
      this.pendingStartLevelIndex = this.clampLevelIndex(levelIndex);
      this.transitionVisible = true;
      this.transitionSeconds = 0.0F;
      this.transitionStartingFromHome = true;
      this.transitionAdvanceLevel = false;
      this.transitionRestartCampaign = false;
      Level level = this.levelManager.getLevel(this.pendingStartLevelIndex);
      this.transitionTitle = "准备进入展厅";
      this.transitionSubtitle = level.getTitle() + " · " + level.getObjective();
   }

   private void finishHomeStartTransition() {
      this.startLevel(this.pendingStartLevelIndex);
      this.pendingStartLevelIndex = -1;
   }

   private void shiftTitlePreviewLevel(int direction) {
      int count = this.levelManager.getLevelCount();
      int current = this.clampLevelIndex(this.levelManager.getCurrentLevelNumber() - 1);
      int next = (current + direction + count) % count;
      this.levelManager.setCurrentLevel(next);
      this.resetLevel(false);
      this.configureViewport(this.screenWidth, this.screenHeight);
   }

   private int clampLevelIndex(int levelIndex) {
      return Math.max(0, Math.min(this.levelManager.getLevelCount() - 1, levelIndex));
   }

   private void update(float deltaSeconds) {
      this.uiElapsedSeconds += deltaSeconds * (this.reduceMotion ? 0.15F : 1.0F);
      if (this.messageSeconds > 0.0F) {
         this.messageSeconds = Math.max(0.0F, this.messageSeconds - deltaSeconds);
      }

      if (this.progressResetConfirmSeconds > 0.0F) {
         this.progressResetConfirmSeconds = Math.max(0.0F, this.progressResetConfirmSeconds - deltaSeconds);
      }

      if (this.transitionVisible) {
         this.transitionSeconds += deltaSeconds;
         if (this.transitionSeconds >= this.transitionDurationSeconds()) {
            if (this.transitionStartingFromHome) {
               this.finishHomeStartTransition();
            } else {
               this.transitionVisible = false;
               this.finishAdvanceAfterClear();
            }
         }

      } else if (this.appScreen == GameView.AppScreen.PLAYING && this.state.isPlaying()) {
         Level level = this.getLevel();
         this.levelElapsedSeconds += deltaSeconds;
         if (this.levelElapsedSeconds >= 360.0F) {
            this.failLevel("超过六分钟仍未撤离，行动窗口关闭。");
         } else {
            if (this.decoySeconds > 0.0F) {
               this.decoySeconds = Math.max(0.0F, this.decoySeconds - deltaSeconds);
            }

            this.updateDisruptors(deltaSeconds);
            boolean boostRequested = this.boostPointerId != -1;
            this.state.update(deltaSeconds, boostRequested, this.upgradedBoostRecoveryMultiplier());
            this.floatingText.update(deltaSeconds);
            this.updatePowerUps(deltaSeconds);
            this.updateCoins();
            float speedMultiplier = this.upgradeSpeedMultiplier() * (this.state.isSpeedPotionActive() ? 2.0F : (this.state.isBoosting() ? 1.5F : 1.0F));
            float inputX = this.joystick.getInputX();
            float inputY = this.joystick.getInputY();
            float dx = inputX * this.player.getSpeed() * speedMultiplier * deltaSeconds;
            float dy = inputY * this.player.getSpeed() * speedMultiplier * deltaSeconds;
            float previousPlayerX = this.player.getX();
            float previousPlayerY = this.player.getY();
            Collision.movePlayer(this.player, dx, dy, level, this.state.canPhaseThroughWalls());
            float movedDistance = (float)Math.hypot(
                  (double)(this.player.getX() - previousPlayerX),
                  (double)(this.player.getY() - previousPlayerY));
            this.player.updateMovementVisuals(
                  inputX, inputY, deltaSeconds, speedMultiplier, movedDistance);

            for(Guard guard : level.getGuards()) {
               guard.setDisabled(guard.getKind() == Kind.ROBOT && this.isInsideDisruptor(guard.getX(), guard.getY()));
               guard.update(deltaSeconds, level);
               boolean seesPlayer = !guard.isDisabled() && !this.state.isInvisible() && this.canDetectorSeePlayer(guard.getKind() == Kind.ROBOT, guard.getX(), guard.getY(), guard.getFacingX(), guard.getFacingY(), guard.getViewDistance(), guard.getViewAngleRadians(), level);
               if (seesPlayer) {
                  float suspicionDelta = deltaSeconds * (guard.getKind() == Kind.ROBOT ? 1.32F : 1.0F);
                  if (guard.addSuspicion(suspicionDelta, this.player.getX(), this.player.getY())) {
                     this.levelResult.addAlert();
                     this.failLevel(guard.getKind() == Kind.STAFF ? "被夜班工作人员认出来了。" : "被巡逻机器人锁定了。");
                     return;
                  }
               } else {
                  float decaySeconds = guard.getKind() == Kind.ROBOT ? 1.224F : 0.9F;
                  guard.decaySuspicion(deltaSeconds, decaySeconds);
                  if (this.decoySeconds > 0.0F && this.isDetectorDistracted(guard.getX(), guard.getY(), guard.getFacingX(), guard.getFacingY(), guard.getViewDistance(), guard.getViewAngleRadians(), level)) {
                     guard.investigate(this.decoyPoint.x, this.decoyPoint.y, 1.6F);
                  }
               }
            }

            for(Laser laser : level.getLasers()) {
               if (laser.isActive(this.levelElapsedSeconds) && Collision.circleIntersectsLine(this.player.getX(), this.player.getY(), this.player.getRadius(), laser.getStartX(), laser.getStartY(), laser.getEndX(), laser.getEndY())) {
                  this.failLevel("触发了正在工作的激光。");
                  return;
               }
            }

            for(SecurityCamera camera : level.getCameras()) {
               camera.setDisabled(this.isInsideDisruptor(camera.getX(), camera.getY()));
               camera.update(deltaSeconds);
               boolean cameraSeesPlayer = !this.state.isInvisible() && !camera.isDisabled() && this.canDetectorSeePlayer(false, camera.getX(), camera.getY(), camera.getFacingX(), camera.getFacingY(), camera.getViewDistance(), camera.getViewAngleRadians(), level);
               if (cameraSeesPlayer) {
                  if (camera.addSuspicion(deltaSeconds)) {
                     this.levelResult.addAlert();
                     this.failLevel("进入了监控扫视范围。");
                     return;
                  }
               } else {
                  camera.decaySuspicion(deltaSeconds, 0.9F);
               }
            }

            float measuredThreat = 0.0F;

            for(Guard guard : level.getGuards()) {
               measuredThreat = Math.max(measuredThreat, guard.getSuspicionProgress());
            }

            for(SecurityCamera camera : level.getCameras()) {
               measuredThreat = Math.max(measuredThreat, camera.getSuspicionProgress());
            }

            this.stealthTracker.update(deltaSeconds, measuredThreat);
            if (this.stealthTracker.consumeDangerEntered()) {
               this.levelResult.addAlert();
               this.feedback.warning(this);
               this.showMessage("警戒升高，立即离开视线！", Color.rgb(240, 93, 72));
            }

            for(KeyItem keyItem : level.getKeyItems()) {
               if (!keyItem.isCollected() && Collision.circleIntersectsRect(this.player.getX(), this.player.getY(), this.player.getRadius(), keyItem.getBounds())) {
                  if (this.state.isHotbarFull() && !this.state.hasKey(keyItem.getKeyCode())) {
                     this.showMessage("物品栏已满，先丢弃一格再拿钥匙。", Color.rgb(235, 88, 80));
                  } else if (!this.state.collectKey(keyItem)) {
                     this.showMessage("物品栏已满，钥匙不会占道具栏。", Color.rgb(235, 88, 80));
                  } else {
                     keyItem.collect();
                     this.feedback.collect(this);
                     this.registerStealthPickup(keyItem.getBounds().centerX(), keyItem.getBounds().top - 10.0F);
                     this.showMessage("取得" + keyItem.getLabel() + "，可开关同色门。", keyItem.getColor());
                  }
               }
            }

            this.updateTreasureClaim(deltaSeconds, level);
            this.updateExitPrompt(deltaSeconds, level);
            this.updateCamera(deltaSeconds, false);
         }
      }
   }

   private void updateExitPrompt(float deltaSeconds, Level level) {
      boolean inExit = Collision.circleIntersectsRect(this.player.getX(), this.player.getY(), this.player.getRadius(), level.getExit());
      if (!inExit) {
         this.resetExitPrompt();
         this.exitPromptSuppressedUntilLeave = false;
      } else if (!ExitRules.canExit(this.state)) {
         this.exitPromptVisible = true;
         this.exitPromptNeedsTreasure = true;
         this.exitStaySeconds = 0.0F;
      } else if (this.exitPromptSuppressedUntilLeave) {
         this.exitPromptVisible = false;
         this.exitPromptNeedsTreasure = false;
         this.exitStaySeconds = 0.0F;
      } else {
         this.exitPromptVisible = true;
         this.exitPromptNeedsTreasure = false;
         this.exitStaySeconds = Math.min(5.0F, this.exitStaySeconds + deltaSeconds);
         if (this.exitStaySeconds >= 5.0F) {
            this.completeLevel();
         }

      }
   }

   private void resetExitPrompt() {
      this.exitPromptVisible = false;
      this.exitPromptNeedsTreasure = false;
      this.exitStaySeconds = 0.0F;
   }

   private void suppressExitPromptUntilLeave() {
      this.resetExitPrompt();
      this.exitPromptSuppressedUntilLeave = true;
   }

   private void updatePowerUps(float deltaSeconds) {
      for(int i = this.powerUps.size() - 1; i >= 0; --i) {
         if (!((PowerUp)this.powerUps.get(i)).update(deltaSeconds)) {
            this.powerUps.remove(i);
         }
      }

      for(int i = this.powerUps.size() - 1; i >= 0; --i) {
         PowerUp powerUp = (PowerUp)this.powerUps.get(i);
         if (Collision.circleIntersectsRect(this.player.getX(), this.player.getY(), this.player.getRadius(), powerUp.getBounds())) {
            if (!this.state.canStorePowerUp()) {
               this.showMessage("道具栏已满，先使用一个再拾取。", Color.rgb(235, 88, 80));
            } else {
               this.state.storePowerUp(powerUp.getType());
               this.powerUps.remove(i);
               this.feedback.collect(this);
               this.floatingText.add(powerUp.getType().getBadge(), powerUp.getBounds().centerX(), powerUp.getBounds().centerY() - 20.0F, this.powerUpColor(powerUp.getType()));
               this.registerStealthPickup(powerUp.getBounds().centerX(), powerUp.getBounds().top - 10.0F);
               this.showMessage("取得道具：" + powerUp.getType().getLabel() + "（" + this.state.getStoredPowerUpCount() + "/2）。", Color.rgb(104, 196, 202));
            }
         }
      }

      if (this.powerUps.size() < 3) {
         this.nextPowerUpSeconds -= deltaSeconds;
         if (this.nextPowerUpSeconds <= 0.0F) {
            this.spawnPowerUpsToTarget();
            this.nextPowerUpSeconds = 6.5F + this.random.nextFloat() * 4.0F;
         }

      }
   }

   private void spawnPowerUpsToTarget() {
      for(int safety = 0; this.powerUps.size() < 3 && safety < 12; ++safety) {
         this.spawnPowerUp();
      }

   }

   private void spawnPowerUp() {
      PointF point = this.itemSpawnSystem.findPowerUpSpawn(this.getLevel(), this.player, this.powerUps, this.coins, this.random);
      if (point != null) {
         PowerUp.Type type = this.itemSpawnSystem.pickPowerUpType(this.random, this.levelElapsedSeconds, this.levelManager.getCurrentLevelNumber());
         this.powerUps.add(new PowerUp(type, point.x, point.y, 28.0F, 8.0F));
      }
   }

   private void spawnCoinsForLevel(Level level) {
      int targetCount = 22 + (this.levelManager.getCurrentLevelNumber() - 1) * 6;

      for(int safety = 0; this.coins.size() < targetCount && safety < targetCount * 5; ++safety) {
         PointF point = this.itemSpawnSystem.findCoinSpawn(level, this.player, this.powerUps, this.coins, this.random);
         if (point == null) {
            break;
         }

         int value = 8 + this.random.nextInt(17);
         this.coins.add(new Coin(point.x, point.y, 15.0F, value));
      }

   }

   private void updateCoins() {
      for(Coin coin : this.coins) {
         if (!coin.isCollected() && Collision.circleIntersectsRect(this.player.getX(), this.player.getY(), this.upgradedCoinPickupRadius(), coin.getBounds())) {
            coin.collect();
            this.addRunCoins(coin.getValue());
            this.feedback.collect(this);
            RectF coinBounds = coin.getBounds();
            this.floatingText.add("+" + coin.getValue(), coinBounds.centerX(), coinBounds.centerY() - 18.0F, Color.rgb(226, 194, 75));
            this.registerStealthPickup(coinBounds.centerX(), coinBounds.top - 8.0F);
            this.showMessage("金币 +" + coin.getValue(), Color.rgb(226, 194, 75));
         }
      }

   }

   private int registerStealthPickup(float worldX, float worldY) {
      int bonus = this.stealthTracker.registerPickup();
      if (bonus > 0) {
         this.addRunCoins(bonus);
         this.floatingText.add("潜行奖励 +" + bonus, worldX, worldY, Color.rgb(246, 210, 105));
      }

      return bonus;
   }

   private void updateDisruptors(float deltaSeconds) {
      for(int i = this.disruptors.size() - 1; i >= 0; --i) {
         if (!((Disruptor)this.disruptors.get(i)).update(deltaSeconds)) {
            this.disruptors.remove(i);
         }
      }

   }

   private boolean isInsideDisruptor(float x, float y) {
      for(Disruptor disruptor : this.disruptors) {
         if (disruptor.contains(x, y)) {
            return true;
         }
      }

      return false;
   }

   private void useStoredPowerUp(int index) {
      PowerUp.Type type = this.state.getStoredPowerUp(index);
      if (!this.state.useStoredPowerUp(index)) {
         this.showMessage(index == 0 ? "左侧道具栏为空。" : "右侧道具栏为空。", Color.rgb(220, 226, 228));
      } else {
         this.consumeRunStashItem(type);
         this.levelResult.addUsedPowerUp();
         if (type == Type.DECOY) {
            this.decoyPoint.set(this.player.getX(), this.player.getY());
            this.decoySeconds = 5.5F;
            this.showMessage("诱饵金币已抛出，安保会短暂分心。", Color.rgb(226, 194, 75));
         } else if (type == Type.JAMMER) {
            this.disruptors.add(new Disruptor(this.player.getX(), this.player.getY(), this.upgradedDisruptorRadius(), 8.0F));
            this.showMessage("干扰装置已部署。", Color.rgb(104, 196, 202));
         } else if (type == Type.SPEED) {
            this.showMessage("加速药剂生效 5 秒。", Color.rgb(126, 204, 104));
         } else {
            this.showMessage(type.getLabel() + "生效 3 秒。", Color.rgb(104, 196, 202));
         }
      }
   }

   private void updateTreasureClaim(float deltaSeconds, Level level) {
      int nearbyIndex = -1;

      for(int i = 0; i < level.getTreasures().size(); ++i) {
         RectF treasure = (RectF)level.getTreasures().get(i);
         if (!this.state.isTreasureCollected(i) && Collision.circleIntersectsRect(this.player.getX(), this.player.getY(), this.player.getRadius(), treasure)) {
            nearbyIndex = i;
            break;
         }
      }

      if (nearbyIndex == -1) {
         this.treasureClaimIndex = -1;
         this.treasureClaimSeconds = 0.0F;
      } else if (this.state.isHotbarFull()) {
         this.treasureClaimIndex = nearbyIndex;
         this.treasureClaimSeconds = 0.0F;
         this.showMessage("物品栏已满，先丢弃一格再取展品。", Color.rgb(235, 88, 80));
      } else {
         if (this.treasureClaimIndex != nearbyIndex) {
            this.treasureClaimIndex = nearbyIndex;
            this.treasureClaimSeconds = 0.0F;
         }

         this.treasureClaimSeconds += deltaSeconds;
         if (this.treasureClaimSeconds >= this.upgradedTreasureClaimSeconds() && this.state.collectTreasure(nearbyIndex)) {
            this.state.addBoostEnergy(0.22F);
            this.addRunCoins(60);
            this.feedback.collect(this);
            RectF treasure = (RectF)level.getTreasures().get(nearbyIndex);
            this.floatingText.add("展品 " + this.state.getCollectedTreasureCount() + "/" + this.state.getTreasureCount(), treasure.centerX(), treasure.top - 8.0F, Color.rgb(213, 168, 79));
            this.registerStealthPickup(treasure.centerX(), treasure.top - 30.0F);
            this.treasureClaimIndex = -1;
            this.treasureClaimSeconds = 0.0F;
            if (this.state.hasAllTreasures()) {
               this.showMessage("三件展品到手，金币 +60。", Color.rgb(213, 168, 79));
            } else {
               this.showMessage("展品入袋：" + this.state.getCollectedTreasureCount() + "/" + this.state.getTreasureCount() + "，金币 +" + 60 + "。", Color.rgb(213, 168, 79));
            }
         }

      }
   }

   private void completeLevel() {
      int levelIndex = this.levelManager.getCurrentLevelNumber() - 1;
      int stars = this.calculateStars();
      int previousBest = levelIndex >= 0 && levelIndex < this.bestStars.length ? this.bestStars[levelIndex] : 0;
      this.levelResult.setStars(stars);
      int totalRewardCoins = LevelScoring.calculateRewardCoins(stars, this.runCoinTotal);
      this.levelResult.setRewardCoins(totalRewardCoins);
      this.levelResult.setCollectedTreasures(this.state.getCollectedTreasureCount());
      this.levelResult.setTotalTreasures(this.state.getTreasureCount());
      this.levelResult.setElapsedSeconds(this.levelElapsedSeconds);
      this.levelResult.setBestStealthChain(this.stealthTracker.getBestChain());
      this.levelResult.setPeakThreat(this.stealthTracker.getPeakThreat());
      this.levelResult.setNewRecord(stars > previousBest);
      this.addCoins(totalRewardCoins);
      this.saveLevelProgress(levelIndex, stars);
      this.resetExitPrompt();
      this.exitPromptSuppressedUntilLeave = false;
      this.state.clearLevel();
      this.feedback.clear(this);
      this.showMessage(stars >= 3 ? "三星撤离，干净利落。" : "成功撤离，可再挑战三星。", Color.rgb(213, 168, 79));
      this.joystick.reset();
      this.boostPointerId = -1;
   }

   private int calculateStars() {
      return LevelScoring.calculateStars(this.state.hasTreasure(), this.state.hasAllTreasures(), this.levelElapsedSeconds, this.levelResult.getAlerts());
   }

   private float upgradeSpeedMultiplier() {
      return this.selectedCharacter.getSpeedMultiplier() + (float)this.upgradeLevels[UpgradeType.SHOES.ordinal()] * 0.05F;
   }

   private float upgradedTreasureClaimSeconds() {
      return 4.0F / (1.0F + (float)this.upgradeLevels[UpgradeType.GLOVES.ordinal()] * 0.1F);
   }

   private float upgradedDisruptorRadius() {
      return 260.0F;
   }

   private float upgradedBoostRecoveryMultiplier() {
      return 1.0F + (float)this.upgradeLevels[UpgradeType.BATTERY.ordinal()] * 0.1F;
   }

   private float upgradedCoinPickupRadius() {
      float baseRadius = this.player.getRadius() + this.selectedCharacter.getPickupRadiusBonus();
      return baseRadius * (1.0F + (float)this.upgradeLevels[UpgradeType.MAGNET.ordinal()] * 0.12F);
   }

   private Door findTouchedDoor(float screenX, float screenY) {
      float worldX = this.cameraX + screenX / this.worldScale;
      float worldY = this.cameraY + screenY / this.worldScale;

      for(Door door : this.getLevel().getDoors()) {
         RectF expanded = new RectF(door.getBounds());
         expanded.inset(-46.0F, -46.0F);
         if (expanded.contains(worldX, worldY) && this.isPlayerNearDoor(door)) {
            return door;
         }
      }

      return null;
   }

   private boolean isPlayerNearDoor(Door door) {
      float dx = door.getBounds().centerX() - this.player.getX();
      float dy = door.getBounds().centerY() - this.player.getY();
      return Math.hypot((double)dx, (double)dy) <= (double)88.0F;
   }

   private void toggleDoor(Door door) {
      if (!this.state.hasKey(door.getKeyCode())) {
         this.showMessage("需要同色钥匙才能操作" + door.getLabel() + "。", door.getColor());
      } else {
         door.toggle();
         this.showMessage(door.getLabel() + (door.isOpen() ? "已打开。" : "已关闭。"), door.getColor());
      }
   }

   private void dropHotbarSlot(int index) {
      GameState.HotbarSlot slot = this.state.getHotbarSlot(index);
      if (slot != null && slot.getType() != HotbarType.EMPTY) {
         if (slot.getType() == HotbarType.KEY) {
            this.dropKeyFromSlot(slot, index);
         } else {
            int treasureIndex = slot.getTreasureIndex();
            if (this.state.dropHotbarSlot(index)) {
               if (this.treasureClaimIndex == treasureIndex) {
                  this.treasureClaimIndex = -1;
                  this.treasureClaimSeconds = 0.0F;
               }

               this.showMessage("已放回展品，需要重新破解展柜。", Color.rgb(213, 168, 79));
            }

         }
      } else {
         this.showMessage("这个栏位是空的。", Color.rgb(220, 226, 228));
      }
   }

   private void dropKeyFromSlot(GameState.HotbarSlot slot, int index) {
      KeyItem matchingKey = null;

      for(KeyItem keyItem : this.getLevel().getKeyItems()) {
         if (keyItem.getKeyCode().equals(slot.getKeyCode())) {
            matchingKey = keyItem;
            break;
         }
      }

      String label = slot.getLabel();
      int color = slot.getColor();
      if (this.state.dropHotbarSlot(index) && matchingKey != null) {
         float dropX = this.clamp(this.player.getX() + 42.0F, this.getLevel().getBounds().left + 34.0F, this.getLevel().getBounds().right - 34.0F);
         float dropY = this.clamp(this.player.getY() + 18.0F, this.getLevel().getBounds().top + 34.0F, this.getLevel().getBounds().bottom - 34.0F);
         matchingKey.dropAt(dropX, dropY);

         for(Door door : this.getLevel().getDoors()) {
            if (door.getKeyCode().equals(matchingKey.getKeyCode()) && door.isOpen()) {
               door.toggle();
            }
         }

         this.showMessage("已丢弃" + label + "，同色门将保持锁定。", color);
      }

   }

   private boolean canDetectorSeePlayer(boolean robotDetector, float originX, float originY, float facingX, float facingY, float viewDistance, float viewAngleRadians, Level level) {
      if (this.state.isInvisible()) {
         return false;
      } else if (this.isDetectorDistracted(originX, originY, facingX, facingY, viewDistance, viewAngleRadians, level)) {
         return false;
      } else {
         float effectiveDistance = viewDistance + (robotDetector ? this.player.getRadius() * 0.85F : this.player.getRadius() * 0.28F);
         float effectiveAngle = viewAngleRadians + (robotDetector ? 0.18F : 0.07F);
         float px = this.player.getX();
         float py = this.player.getY();
         float r = this.player.getRadius() * (robotDetector ? 0.82F : 0.5F);
         float lead = robotDetector ? this.player.getRadius() * 0.62F : this.player.getRadius() * 0.2F;
         float diagonal = r * 0.62F;
         float clearance = this.player.getRadius() * (robotDetector ? 0.22F : 0.35F);
         return this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px, py, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px + r, py, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px - r, py, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px, py + r, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px, py - r, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px + this.player.getFacingX() * lead, py + this.player.getFacingY() * lead, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px + diagonal, py + diagonal, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px - diagonal, py + diagonal, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px + diagonal, py - diagonal, level, clearance) || this.canSeePlayerSample(originX, originY, facingX, facingY, effectiveDistance, effectiveAngle, px - diagonal, py - diagonal, level, clearance);
      }
   }

   private boolean canSeePlayerSample(float ox, float oy, float fx, float fy, float distance, float angle, float x, float y, Level level, float clearance) {
      return Vision.canSee(ox, oy, fx, fy, distance, angle, x, y) && !Collision.lineBlockedByLevel(ox, oy, x, y, level, clearance);
   }

   private boolean isDetectorDistracted(float originX, float originY, float facingX, float facingY, float viewDistance, float viewAngleRadians, Level level) {
      return this.decoySeconds > 0.0F && Vision.canSee(originX, originY, facingX, facingY, viewDistance, viewAngleRadians, this.decoyPoint.x, this.decoyPoint.y) && !Collision.lineBlockedByLevel(originX, originY, this.decoyPoint.x, this.decoyPoint.y, level);
   }

   private void updateCamera(float deltaSeconds, boolean immediate) {
      Level level = this.getLevel();
      float visibleWidth = (float)this.screenWidth / Math.max(this.worldScale, 0.001F);
      float visibleHeight = (float)this.screenHeight / Math.max(this.worldScale, 0.001F);
      float targetX = this.player.getX() - visibleWidth * 0.5F;
      float targetY = this.player.getY() - visibleHeight * 0.5F;
      float maxX = Math.max(0.0F, level.getWorldWidth() - visibleWidth);
      float maxY = Math.max(0.0F, level.getWorldHeight() - visibleHeight);
      targetX = this.clamp(targetX, 0.0F, maxX);
      targetY = this.clamp(targetY, 0.0F, maxY);
      if (immediate) {
         this.cameraX = targetX;
         this.cameraY = targetY;
      } else {
         float follow = this.reduceMotion ? 1.0F : 1.0F - (float)Math.exp((double)(-8.5F * Math.max(0.0F, deltaSeconds)));
         this.cameraX += (targetX - this.cameraX) * follow;
         this.cameraY += (targetY - this.cameraY) * follow;
      }
   }

   private float transitionDurationSeconds() {
      return this.reduceMotion ? 0.15F : 1.35F;
   }

   private void failLevel(String reason) {
      this.resetExitPrompt();
      this.exitPromptSuppressedUntilLeave = false;
      this.state.fail(reason);
      this.feedback.fail(this);
      this.showMessage(reason, Color.rgb(235, 88, 80));
      this.joystick.reset();
   }

   private void showMessage(String text, int color) {
      this.messageText = text;
      this.messageColor = color;
      this.messageSeconds = 2.2F;
   }

   private void drawFrame() {
      Canvas canvas;
      try {
         canvas = this.holder.lockCanvas();
      } catch (IllegalStateException | IllegalArgumentException var14) {
         return;
      }

      if (canvas != null) {
         try {
            canvas.drawColor(Palette.SCREEN_BASE);
            if (this.appScreen == GameView.AppScreen.TITLE) {
               this.drawTitleScreen(canvas);
            } else if (this.appScreen == GameView.AppScreen.CHARACTER_SELECT) {
               this.drawCharacterSelectScreen(canvas);
            } else if (this.appScreen == GameView.AppScreen.LEVEL_SELECT) {
               this.drawLevelSelectScreen(canvas);
            } else if (this.appScreen == GameView.AppScreen.SHOP) {
               this.drawShopScreen(canvas);
            } else if (this.appScreen == GameView.AppScreen.SETTINGS) {
               this.drawSettingsScreen(canvas);
            } else {
               this.drawGameScreen(canvas);
            }
         } catch (RuntimeException exception) {
            Log.e("MuseumHeistGameView", "Frame rendering failed", exception);
            this.showMessage("画面已自动恢复。", Color.rgb(230, 236, 238));
         } finally {
            try {
               this.holder.unlockCanvasAndPost(canvas);
            } catch (IllegalStateException | IllegalArgumentException var11) {
            }

         }

      }
   }

   private void drawGameScreen(Canvas canvas) {
      canvas.save();
      canvas.clipRect(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight());
      canvas.scale(this.worldScale, this.worldScale);
      canvas.translate(-this.cameraX, -this.cameraY);
      this.drawMuseum(canvas);
      canvas.restore();
      this.drawScreenVignette(canvas);
      this.drawThreatVignette(canvas);
      this.drawHudPanelV10(canvas);
      this.drawTopButtons(canvas);
      this.drawMiniMap(canvas);
      this.drawInventoryBar(canvas);
      this.drawTimerPanelV10(canvas);
      this.drawMessageBanner(canvas);
      this.drawJoystick(canvas);
      this.drawActionButtons(canvas);
      this.drawExitPrompt(canvas);
      this.drawOverlay(canvas);
      this.drawTransitionOverlay(canvas);
   }

   private void drawTitleScreen(Canvas canvas) {
      this.drawScreenBackdrop(canvas, Color.rgb(15, 25, 33), Color.rgb(73, 139, 143));
      float sharedTop = (float)this.getHeight() * 0.06F;
      float sharedBottom = (float)this.getHeight() - 24.0F * this.uiScale;
      RectF heroPanel = new RectF(28.0F * this.uiScale, sharedTop, Math.min((float)this.getWidth() * 0.63F, this.titleStartButton.left - 28.0F * this.uiScale), sharedBottom);
      RectF actionPanel = new RectF(this.titleStartButton.left - 24.0F * this.uiScale, sharedTop, this.titleStartButton.right + 24.0F * this.uiScale, sharedBottom);
      this.drawHomeCharacterPanel(canvas, heroPanel);
      this.sceneRect.set(actionPanel.left + 10.0F * this.uiScale, actionPanel.top + 12.0F * this.uiScale, actionPanel.right + 10.0F * this.uiScale, actionPanel.bottom + 12.0F * this.uiScale);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(90, 0, 0, 0));
      canvas.drawRoundRect(this.sceneRect, 16.0F * this.uiScale, 16.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(24, 38, 47));
      canvas.drawRoundRect(actionPanel, 16.0F * this.uiScale, 16.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(78, 205, 197));
      this.sceneRect.set(actionPanel.left, actionPanel.top, actionPanel.left + 7.0F * this.uiScale, actionPanel.bottom);
      canvas.drawRoundRect(this.sceneRect, 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.8F * this.uiScale);
      this.paint.setColor(Color.argb(185, 93, 179, 184));
      canvas.drawRoundRect(actionPanel, 16.0F * this.uiScale, 16.0F * this.uiScale, this.paint);
      this.drawLeftFittedText(canvas, "NIGHT MUSEUM OPS", actionPanel.left + 28.0F * this.uiScale, actionPanel.top + 35.0F * this.uiScale, actionPanel.width() - 56.0F * this.uiScale, 18.0F * this.uiScale, Color.rgb(101, 222, 214));
      this.drawCenteredFittedText(canvas, "行动设置", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.094F, (float)this.getWidth() * 0.74F, 44.0F * this.uiScale, Color.rgb(241, 246, 244));
      this.drawTitleStatChips(canvas, actionPanel);
      this.drawPrimaryButton(canvas, this.titleStartButton, "继续行动");
      this.drawButton(canvas, this.titleCharacterButton, "更换角色");
      this.drawButton(canvas, this.titleSelectButton, "选择展厅");
      this.drawButton(canvas, this.titleShopButton, "装备商店");
      this.drawButton(canvas, this.titleSettingsButton, "设置");
      this.drawTitleProgress(canvas);
      this.drawTitleOpenAccessBrief(canvas);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(150, 10, 19, 25));
      canvas.drawRoundRect(this.titleFooterPanel, 7.0F * this.uiScale, 7.0F * this.uiScale, this.paint);
      this.drawCenteredFittedText(canvas, "版本 4.1.0 · 当前角色：" + this.selectedCharacter.getName(), this.titleFooterPanel.centerX(), this.titleFooterPanel.centerY() + 6.0F * this.uiScale, this.titleFooterPanel.width() - 18.0F * this.uiScale, 16.0F * this.uiScale, Color.rgb(218, 231, 232));
      this.drawMapConfirmOverlay(canvas);
      this.drawTransitionOverlay(canvas);
      this.paint.setTextAlign(Align.LEFT);
   }

   private void drawHomeCharacterPanel(Canvas canvas, RectF panel) {
      this.sceneRect.set(panel.left + 11.0F * this.uiScale, panel.top + 13.0F * this.uiScale, panel.right + 11.0F * this.uiScale, panel.bottom + 13.0F * this.uiScale);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(86, 0, 0, 0));
      canvas.drawRoundRect(this.sceneRect, 18.0F * this.uiScale, 18.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(22, 35, 43));
      canvas.drawRoundRect(panel, 18.0F * this.uiScale, 18.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.2F * this.uiScale);
      this.paint.setColor(Color.argb(210, Color.red(this.selectedCharacter.getAccentColor()), Color.green(this.selectedCharacter.getAccentColor()), Color.blue(this.selectedCharacter.getAccentColor())));
      canvas.drawRoundRect(panel, 18.0F * this.uiScale, 18.0F * this.uiScale, this.paint);
      RectF portrait = new RectF(panel.left + panel.width() * 0.035F, panel.top + panel.height() * 0.065F, panel.left + panel.width() * 0.405F, panel.bottom - panel.height() * 0.065F);
      RectF info = new RectF(panel.left + panel.width() * 0.425F, panel.top + panel.height() * 0.065F, panel.right - 22.0F * this.uiScale, panel.top + panel.height() * 0.385F);
      RectF map = new RectF(panel.left + panel.width() * 0.425F, panel.top + panel.height() * 0.415F, panel.right - 22.0F * this.uiScale, panel.bottom - 22.0F * this.uiScale);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(29, 47, 56));
      canvas.drawRoundRect(portrait, 20.0F * this.uiScale, 20.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(54, Color.red(this.selectedCharacter.getAccentColor()), Color.green(this.selectedCharacter.getAccentColor()), Color.blue(this.selectedCharacter.getAccentColor())));
      canvas.drawCircle(portrait.centerX(), portrait.centerY() - portrait.height() * 0.02F, Math.min(portrait.width(), portrait.height()) * 0.42F, this.paint);
      this.sceneRect.set(portrait.left + portrait.width() * 0.12F, portrait.bottom - portrait.height() * 0.16F, portrait.right - portrait.width() * 0.12F, portrait.bottom - portrait.height() * 0.055F);
      this.paint.setColor(Color.argb(95, 0, 0, 0));
      canvas.drawOval(this.sceneRect, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.6F * this.uiScale);
      this.paint.setColor(Color.argb(150, 154, 207, 205));
      canvas.drawRoundRect(portrait, 20.0F * this.uiScale, 20.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.characterRenderer.drawFullBody(canvas, this.selectedCharacter, portrait, this.uiElapsedSeconds, true);
      this.paint.setColor(Color.rgb(30, 46, 54));
      canvas.drawRoundRect(info, 14.0F * this.uiScale, 14.0F * this.uiScale, this.paint);
      this.paint.setColor(this.selectedCharacter.getAccentColor());
      this.sceneRect.set(info.left, info.top, info.left + 6.0F * this.uiScale, info.bottom);
      canvas.drawRoundRect(this.sceneRect, 3.0F * this.uiScale, 3.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.3F * this.uiScale);
      this.paint.setColor(Color.argb(130, 150, 205, 204));
      canvas.drawRoundRect(info, 14.0F * this.uiScale, 14.0F * this.uiScale, this.paint);
      this.drawLeftFittedText(canvas, "当前行动角色", info.left + 20.0F * this.uiScale, info.top + 27.0F * this.uiScale, info.width() - 36.0F * this.uiScale, 17.0F * this.uiScale, Color.rgb(101, 222, 214));
      this.drawLeftFittedText(canvas, this.selectedCharacter.getName(), info.left + 20.0F * this.uiScale, info.top + 72.0F * this.uiScale, info.width() - 36.0F * this.uiScale, 37.0F * this.uiScale, -1);
      this.drawLeftFittedText(canvas, this.selectedCharacter.getStyleTag() + " · 重制步态", info.left + 20.0F * this.uiScale, info.top + 107.0F * this.uiScale, info.width() - 36.0F * this.uiScale, 19.0F * this.uiScale, this.selectedCharacter.getAccentColor());
      this.drawWrappedCenteredText(canvas, this.selectedCharacter.getDescription(), info.centerX(), info.top + 127.0F * this.uiScale, info.width() - 42.0F * this.uiScale, 16.0F * this.uiScale, 20.0F * this.uiScale, 2, Color.rgb(211, 224, 225));
      this.drawTitleMapCarousel(canvas, map);
   }

   private void drawTitleOpenAccessBrief(Canvas canvas) {
      float top = this.titleProgressPanel.bottom + 9.0F * this.uiScale;
      float bottom = this.titleFooterPanel.top - 9.0F * this.uiScale;
      if (!(bottom - top < 34.0F * this.uiScale)) {
         RectF brief = new RectF(this.titleStartButton.left, top, this.titleStartButton.right, bottom);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(138, 10, 20, 27));
         canvas.drawRoundRect(brief, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(1.3F * this.uiScale);
         this.paint.setColor(Color.argb(125, 78, 205, 197));
         canvas.drawRoundRect(brief, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
         float headingY = brief.top + Math.min(31.0F * this.uiScale, brief.height() * 0.32F);
         this.drawLeftFittedText(canvas, "全馆开放行动", brief.left + 16.0F * this.uiScale, headingY, brief.width() - 32.0F * this.uiScale, 19.0F * this.uiScale, Color.rgb(101, 222, 214));
         if (brief.height() < 72.0F * this.uiScale) {
            this.drawCenteredFittedText(canvas, "10 间展厅可直接进入 · 星级仅记录挑战表现", brief.centerX(), brief.bottom - 12.0F * this.uiScale, brief.width() - 24.0F * this.uiScale, 14.0F * this.uiScale, Color.rgb(218, 231, 232));
         } else {
            String[] labels = new String[]{"自由选厅", "撤离规则", "安保升级"};
            String[] values = new String[]{"无需前置通关", "至少携带 1 件藏品", "大地图 / 强巡逻 / 激光监控"};
            float rowsTop = brief.top + 43.0F * this.uiScale;
            float rowHeight = Math.min(40.0F * this.uiScale, (brief.bottom - rowsTop - 8.0F * this.uiScale) / (float)labels.length);

            for(int i = 0; i < labels.length; ++i) {
               float centerY = rowsTop + rowHeight * ((float)i + 0.5F);
               if (centerY + rowHeight * 0.5F > brief.bottom) {
                  break;
               }

               this.paint.setStyle(Style.FILL);
               this.paint.setColor(i % 2 == 0 ? Color.argb(42, 78, 205, 197) : Color.argb(28, 255, 255, 255));
               this.sceneRect.set(brief.left + 10.0F * this.uiScale, centerY - rowHeight * 0.42F, brief.right - 10.0F * this.uiScale, centerY + rowHeight * 0.42F);
               canvas.drawRoundRect(this.sceneRect, 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
               this.drawLeftFittedText(canvas, labels[i], this.sceneRect.left + 10.0F * this.uiScale, centerY + 5.0F * this.uiScale, this.sceneRect.width() * 0.3F, 15.0F * this.uiScale, Color.rgb(238, 242, 240));
               this.drawLeftFittedText(canvas, values[i], this.sceneRect.left + this.sceneRect.width() * 0.32F, centerY + 5.0F * this.uiScale, this.sceneRect.width() * 0.64F, 14.5F * this.uiScale, Color.rgb(188, 211, 213));
            }

         }
      }
   }

   private void drawTitleMapCarousel(Canvas canvas, RectF bounds) {
      this.titleMapPreviewButton.set(bounds);
      float arrowSize = Math.min(48.0F * this.uiScale, bounds.height() * 0.22F);
      this.titleMapPrevButton.set(bounds.left + 12.0F * this.uiScale, bounds.centerY() - arrowSize * 0.5F, bounds.left + 12.0F * this.uiScale + arrowSize, bounds.centerY() + arrowSize * 0.5F);
      this.titleMapNextButton.set(bounds.right - 12.0F * this.uiScale - arrowSize, bounds.centerY() - arrowSize * 0.5F, bounds.right - 12.0F * this.uiScale, bounds.centerY() + arrowSize * 0.5F);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(70, 0, 0, 0));
      canvas.drawRoundRect(new RectF(bounds.left + 8.0F * this.uiScale, bounds.top + 10.0F * this.uiScale, bounds.right + 8.0F * this.uiScale, bounds.bottom + 10.0F * this.uiScale), 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(17, 26, 31));
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.2F * this.uiScale);
      this.paint.setColor(Color.rgb(126, 204, 104));
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      int levelIndex = this.levelManager.getCurrentLevelNumber() - 1;
      Level level = this.levelManager.getLevel(levelIndex);
      RectF map = new RectF(bounds.left + 54.0F * this.uiScale, bounds.top + 46.0F * this.uiScale, bounds.right - 54.0F * this.uiScale, bounds.bottom - 54.0F * this.uiScale);
      this.drawLevelPreviewMap(canvas, map, level, levelIndex);
      this.drawLeftFittedText(canvas, "展厅预览 " + (levelIndex + 1) + "/" + this.levelManager.getLevelCount(), bounds.left + 18.0F * this.uiScale, bounds.top + 24.0F * this.uiScale, bounds.width() * 0.45F, 16.0F * this.uiScale, Color.rgb(104, 196, 202));
      this.drawCenteredFittedText(canvas, level.getTitle(), bounds.centerX(), bounds.top + 25.0F * this.uiScale, bounds.width() * 0.4F, 18.0F * this.uiScale, -1);
      this.drawCenteredFittedText(canvas, "点击预览进入确认", bounds.centerX(), bounds.bottom - 18.0F * this.uiScale, bounds.width() * 0.5F, 15.0F * this.uiScale, Color.rgb(220, 226, 228));
      this.drawMapPreviewStars(canvas, bounds, this.bestStars[levelIndex]);
      this.drawCarouselArrow(canvas, this.titleMapPrevButton, false);
      this.drawCarouselArrow(canvas, this.titleMapNextButton, true);
   }

   private void drawMapPreviewStars(Canvas canvas, RectF bounds, int stars) {
      float startX = bounds.right - 154.0F * this.uiScale;
      float y = bounds.top - 54.0F * this.uiScale;
      float spacing = 34.0F * this.uiScale;

      for(int i = 0; i < 3; ++i) {
         this.drawStar(canvas, startX + (float)i * spacing, y, 15.0F * this.uiScale, i < stars, Color.rgb(213, 168, 79));
      }

   }

   private void drawLevelPreviewMap(Canvas canvas, RectF target, Level level, int levelIndex) {
      RectF levelBounds = level.getBounds();
      float scaleX = target.width() / level.getWorldWidth();
      float scaleY = target.height() / level.getWorldHeight();
      float scale = Math.min(scaleX, scaleY);
      float left = target.left + (target.width() - level.getWorldWidth() * scale) * 0.5F;
      float top = target.top + (target.height() - level.getWorldHeight() * scale) * 0.5F;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(this.levelFloorColor(levelIndex));
      canvas.drawRoundRect(target, 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(50, 255, 255, 255));
      canvas.drawRect(left + levelBounds.left * scale, top + levelBounds.top * scale, left + levelBounds.right * scale, top + (levelBounds.top + 30.0F) * scale, this.paint);
      this.paint.setColor(this.levelCarpetColor(levelIndex));
      RectF carpet = new RectF(left + (levelBounds.left + levelBounds.width() * 0.14F) * scale, top + (levelBounds.centerY() - 32.0F) * scale, left + (levelBounds.right - levelBounds.width() * 0.14F) * scale, top + (levelBounds.centerY() + 32.0F) * scale);
      canvas.drawRoundRect(carpet, 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      this.paint.setColor(this.levelWallDarkColor(levelIndex));

      for(Wall wall : level.getWalls()) {
         RectF wallBounds = wall.getBounds();
         canvas.drawRoundRect(new RectF(left + wallBounds.left * scale, top + wallBounds.top * scale, left + wallBounds.right * scale, top + wallBounds.bottom * scale), 3.0F * this.uiScale, 3.0F * this.uiScale, this.paint);
      }

      this.paint.setColor(Color.rgb(58, 153, 105));
      RectF exit = level.getExit();
      canvas.drawRoundRect(new RectF(left + exit.left * scale, top + exit.top * scale, left + exit.right * scale, top + exit.bottom * scale), 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);

      for(int i = 0; i < level.getTreasures().size(); ++i) {
         RectF treasure = (RectF)level.getTreasures().get(i);
         this.paint.setColor(this.exhibitColor(i));
         canvas.drawCircle(left + treasure.centerX() * scale, top + treasure.centerY() * scale, Math.max(4.0F * this.uiScale, 12.0F * scale), this.paint);
      }

      this.paint.setColor(Color.rgb(151, 54, 54));

      for(Guard guard : level.getGuards()) {
         canvas.drawCircle(left + guard.getX() * scale, top + guard.getY() * scale, Math.max(3.5F * this.uiScale, 10.0F * scale), this.paint);
      }

      this.paint.setColor(Color.rgb(38, 111, 190));
      canvas.drawCircle(left + level.getPlayerStartX() * scale, top + level.getPlayerStartY() * scale, Math.max(5.0F * this.uiScale, 13.0F * scale), this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.5F * this.uiScale);
      this.paint.setColor(Color.argb(130, 255, 255, 255));
      canvas.drawRoundRect(target, 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
   }

   private void drawCarouselArrow(Canvas canvas, RectF bounds, boolean next) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(188, 8, 14, 18));
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.4F * this.uiScale);
      this.paint.setColor(Color.argb(150, 126, 204, 104));
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(238, 244, 242));
      this.visionPath.reset();
      float cx = bounds.centerX();
      float cy = bounds.centerY();
      float s = bounds.width() * 0.23F;
      if (next) {
         this.visionPath.moveTo(cx - s * 0.45F, cy - s);
         this.visionPath.lineTo(cx + s * 0.55F, cy);
         this.visionPath.lineTo(cx - s * 0.45F, cy + s);
      } else {
         this.visionPath.moveTo(cx + s * 0.45F, cy - s);
         this.visionPath.lineTo(cx - s * 0.55F, cy);
         this.visionPath.lineTo(cx + s * 0.45F, cy + s);
      }

      this.visionPath.close();
      canvas.drawPath(this.visionPath, this.paint);
   }

   private void drawMapConfirmOverlay(Canvas canvas) {
      if (this.mapConfirmVisible && (this.appScreen == GameView.AppScreen.TITLE || this.appScreen == GameView.AppScreen.LEVEL_SELECT)) {
         int levelIndex = this.clampLevelIndex(this.pendingStartLevelIndex);
         Level level = this.levelManager.getLevel(levelIndex);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(205, 3, 8, 12));
         canvas.drawRect(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight(), this.paint);
         RectF panel = this.mapConfirmPanel;
         this.sceneRect.set(panel.left + 10.0F * this.uiScale, panel.top + 12.0F * this.uiScale, panel.right + 10.0F * this.uiScale, panel.bottom + 12.0F * this.uiScale);
         this.paint.setColor(Color.argb(105, 0, 0, 0));
         canvas.drawRoundRect(this.sceneRect, 17.0F * this.uiScale, 17.0F * this.uiScale, this.paint);
         this.paint.setColor(Color.rgb(24, 38, 47));
         canvas.drawRoundRect(panel, 17.0F * this.uiScale, 17.0F * this.uiScale, this.paint);
         this.paint.setColor(Color.rgb(76, 205, 196));
         this.sceneRect.set(panel.left, panel.top, panel.right, panel.top + 7.0F * this.uiScale);
         canvas.drawRoundRect(this.sceneRect, 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(2.2F * this.uiScale);
         this.paint.setColor(Color.rgb(100, 220, 210));
         canvas.drawRoundRect(panel, 17.0F * this.uiScale, 17.0F * this.uiScale, this.paint);
         this.drawCenteredFittedText(canvas, "行动部署", panel.centerX(), panel.top + 42.0F * this.uiScale, panel.width() - 44.0F * this.uiScale, 30.0F * this.uiScale, -1);
         this.drawCenteredFittedText(canvas, "展厅 " + (levelIndex + 1) + " · " + level.getTitle(), panel.centerX(), panel.top + 78.0F * this.uiScale, panel.width() - 48.0F * this.uiScale, 21.0F * this.uiScale, Color.rgb(242, 203, 92));
         this.drawCenteredFittedText(canvas, level.getObjective(), panel.centerX(), panel.top + 111.0F * this.uiScale, panel.width() - 58.0F * this.uiScale, 16.0F * this.uiScale, Color.rgb(218, 231, 232));
         this.drawCenteredFittedText(canvas, "行动角色：" + this.selectedCharacter.getName(), panel.centerX(), panel.top + 143.0F * this.uiScale, panel.width() - 58.0F * this.uiScale, 16.0F * this.uiScale, this.selectedCharacter.getAccentColor());
         this.drawCenteredFittedText(canvas, "选择装备 · 最多携带两件", panel.centerX(), panel.top + 177.0F * this.uiScale, panel.width() - 58.0F * this.uiScale, 17.0F * this.uiScale, Color.rgb(101, 222, 214));

         for(int i = 0; i < this.loadoutItemButtons.size() && i < SHOP_TYPES.length; ++i) {
            this.drawLoadoutOption(canvas, (RectF)this.loadoutItemButtons.get(i), SHOP_TYPES[i]);
         }

         String first = this.loadoutSelection.get(0) == null ? "空槽" : this.loadoutSelection.get(0).getLabel();
         String second = this.loadoutSelection.get(1) == null ? "空槽" : this.loadoutSelection.get(1).getLabel();
         this.drawCenteredFittedText(canvas, "装备：" + first + " / " + second, panel.centerX(), panel.top + 322.0F * this.uiScale, panel.width() - 58.0F * this.uiScale, 16.0F * this.uiScale, Color.rgb(232, 239, 239));
         this.drawDialogButton(canvas, this.mapConfirmCancelButton, "返回", Color.rgb(35, 48, 57), Color.rgb(96, 199, 201));
         this.drawDialogButton(canvas, this.mapConfirmStartButton, "开始行动", Color.rgb(42, 112, 82), Color.rgb(126, 224, 156));
      }
   }

   private void drawLoadoutOption(Canvas canvas, RectF bounds, PowerUp.Type type) {
      boolean selected = this.loadoutSelection.contains(type);
      int stock = this.shopStock[type.ordinal()];
      int accent = this.powerUpColor(type);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(selected ? Color.rgb(35, 74, 67) : (stock > 0 ? Color.rgb(34, 49, 58) : Color.rgb(43, 47, 51)));
      canvas.drawRoundRect(bounds, 11.0F * this.uiScale, 11.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(selected ? 70 : 30, Color.red(accent), Color.green(accent), Color.blue(accent)));
      this.sceneRect.set(bounds.left + 5.0F * this.uiScale, bounds.top + 5.0F * this.uiScale, bounds.right - 5.0F * this.uiScale, bounds.top + 45.0F * this.uiScale);
      canvas.drawRoundRect(this.sceneRect, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth((selected ? 2.7F : 1.5F) * this.uiScale);
      this.paint.setColor(selected ? accent : this.withAlpha(accent, stock > 0 ? 175 : 80));
      canvas.drawRoundRect(bounds, 11.0F * this.uiScale, 11.0F * this.uiScale, this.paint);
      this.drawPowerUpSymbol(canvas, type, bounds.centerX(), bounds.top + 29.0F * this.uiScale, 15.0F * this.uiScale, !selected && stock <= 0 ? -7829368 : accent);
      this.drawCenteredFittedText(canvas, type.getLabel(), bounds.centerX(), bounds.top + 61.0F * this.uiScale, bounds.width() - 10.0F * this.uiScale, 13.5F * this.uiScale, -1);
      this.drawCenteredFittedText(canvas, selected ? "已装备" : "库存 " + stock, bounds.centerX(), bounds.bottom - 9.0F * this.uiScale, bounds.width() - 10.0F * this.uiScale, 12.0F * this.uiScale, selected ? Color.rgb(139, 231, 165) : Color.rgb(204, 216, 218));
   }

   private void drawDialogButton(Canvas canvas, RectF bounds, String label, int fillColor, int accentColor) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(fillColor);
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.6F * this.uiScale);
      this.paint.setColor(accentColor);
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.drawCenteredFittedText(canvas, label, bounds.centerX(), bounds.centerY() + 7.0F * this.uiScale, bounds.width() - 16.0F * this.uiScale, 19.0F * this.uiScale, -1);
   }

   private void drawTransitionOverlay(Canvas canvas) {
      if (this.transitionVisible) {
         float progress = this.clamp(this.transitionSeconds / this.transitionDurationSeconds(), 0.0F, 1.0F);
         int alpha = (int)((double)185.0F + Math.sin((double)progress * Math.PI) * (double)32.0F);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(alpha, 3, 6, 9));
         canvas.drawRect(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight(), this.paint);
         RectF panel = new RectF((float)this.getWidth() * 0.5F - Math.min(430.0F * this.uiScale, (float)this.getWidth() * 0.36F), (float)this.getHeight() * 0.34F, (float)this.getWidth() * 0.5F + Math.min(430.0F * this.uiScale, (float)this.getWidth() * 0.36F), (float)this.getHeight() * 0.64F);
         this.paint.setColor(Color.argb(220, 12, 18, 24));
         canvas.drawRoundRect(panel, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(1.8F * this.uiScale);
         this.paint.setColor(Color.rgb(104, 196, 202));
         canvas.drawRoundRect(panel, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
         float pulse = (float)Math.sin((double)(this.uiElapsedSeconds * 7.0F)) * 0.5F + 0.5F;
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb((int)(70.0F + pulse * 45.0F), 126, 204, 104));
         canvas.drawCircle(panel.centerX(), panel.top + 52.0F * this.uiScale, 24.0F * this.uiScale + pulse * 4.0F * this.uiScale, this.paint);
         this.drawCenteredFittedText(canvas, this.transitionTitle, panel.centerX(), panel.top + 116.0F * this.uiScale, panel.width() - 48.0F * this.uiScale, 32.0F * this.uiScale, -1);
         this.drawCenteredFittedText(canvas, this.transitionSubtitle, panel.centerX(), panel.top + 154.0F * this.uiScale, panel.width() - 56.0F * this.uiScale, 17.0F * this.uiScale, Color.rgb(220, 226, 228));
         RectF track = new RectF(panel.left + 44.0F * this.uiScale, panel.bottom - 40.0F * this.uiScale, panel.right - 44.0F * this.uiScale, panel.bottom - 30.0F * this.uiScale);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(130, 238, 244, 242));
         canvas.drawRoundRect(track, 5.0F * this.uiScale, 5.0F * this.uiScale, this.paint);
         this.paint.setColor(Color.rgb(126, 204, 104));
         canvas.drawRoundRect(new RectF(track.left, track.top, track.left + track.width() * progress, track.bottom), 5.0F * this.uiScale, 5.0F * this.uiScale, this.paint);
      }
   }

   private void drawCollectionPreview(Canvas canvas, RectF actionPanel) {
      int perfect = 0;

      for(int stars : this.bestStars) {
         if (stars >= 3) {
            ++perfect;
         }
      }

      float y = this.titleSettingsButton.bottom + 22.0F * this.uiScale;
      float left = actionPanel.left + 24.0F * this.uiScale;
      float right = actionPanel.right - 24.0F * this.uiScale;
      RectF shelf = new RectF(left, y, right, y + 68.0F * this.uiScale);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(76, 213, 168, 79));
      canvas.drawRoundRect(shelf, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.4F * this.uiScale);
      this.paint.setColor(Color.argb(125, 213, 168, 79));
      canvas.drawRoundRect(shelf, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.drawLeftFittedText(canvas, "收藏室雏形", shelf.left + 12.0F * this.uiScale, shelf.centerY() + 6.0F * this.uiScale, shelf.width() * 0.34F, 16.0F * this.uiScale, Color.rgb(238, 240, 232));
      this.drawCenteredFittedText(canvas, perfect + "/" + this.levelManager.getLevelCount() + " 件三星藏品点亮", shelf.centerX() + 44.0F * this.uiScale, shelf.centerY() + 6.0F * this.uiScale, shelf.width() * 0.58F, 16.0F * this.uiScale, Color.rgb(238, 240, 232));
   }

   private void drawTitleCollectionSummary(Canvas canvas, RectF shelf, int perfect) {
      int totalStars = 0;

      for(int stars : this.bestStars) {
         totalStars += stars;
      }

      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(178, 8, 12, 17));
      canvas.drawRoundRect(new RectF(shelf.left + 8.0F * this.uiScale, shelf.top + 8.0F * this.uiScale, shelf.right - 8.0F * this.uiScale, shelf.bottom - 8.0F * this.uiScale), 7.0F * this.uiScale, 7.0F * this.uiScale, this.paint);
      this.drawLeftFittedText(canvas, "收藏进度", shelf.left + 18.0F * this.uiScale, shelf.top + 28.0F * this.uiScale, shelf.width() * 0.38F, 18.0F * this.uiScale, Color.rgb(238, 240, 232));
      this.drawCenteredFittedText(canvas, totalStars + "/" + this.levelManager.getLevelCount() * 3 + " 星", shelf.right - 78.0F * this.uiScale, shelf.top + 28.0F * this.uiScale, 128.0F * this.uiScale, 19.0F * this.uiScale, Color.rgb(226, 194, 75));
      this.drawLeftFittedText(canvas, perfect + "/" + this.levelManager.getLevelCount() + " 间三星藏品点亮", shelf.left + 18.0F * this.uiScale, shelf.top + 54.0F * this.uiScale, shelf.width() - 36.0F * this.uiScale, 16.0F * this.uiScale, Color.rgb(238, 240, 232));
   }

   private void drawTitleCollectionSummaryOverlay(Canvas canvas, RectF actionPanel) {
      int perfect = 0;

      for(int stars : this.bestStars) {
         if (stars >= 3) {
            ++perfect;
         }
      }

      float y = this.titleSettingsButton.bottom + 22.0F * this.uiScale;
      RectF shelf = new RectF(actionPanel.left + 24.0F * this.uiScale, y, actionPanel.right - 24.0F * this.uiScale, y + 68.0F * this.uiScale);
      this.drawTitleCollectionSummary(canvas, shelf, perfect);
   }

   private void drawTitleStatChips(Canvas canvas, RectF actionPanel) {
      String[] labels = new String[]{"6:00 撤离", "全馆开放", "强化安保"};
      int[] colors = new int[]{Color.rgb(104, 196, 202), Color.rgb(226, 194, 75), Color.rgb(58, 153, 105)};
      float horizontalPadding = 18.0F * this.uiScale;
      float gap = 8.0F * this.uiScale;
      float chipWidth = (actionPanel.width() - horizontalPadding * 2.0F - gap * (float)(labels.length - 1)) / (float)labels.length;
      float chipBottom = this.titleStartButton.top - 10.0F * this.uiScale;
      float availableHeight = chipBottom - (actionPanel.top + 124.0F * this.uiScale);
      float chipHeight = this.clamp(availableHeight, 24.0F * this.uiScale, 32.0F * this.uiScale);
      float chipTop = chipBottom - chipHeight;

      for(int i = 0; i < labels.length; ++i) {
         float left = actionPanel.left + horizontalPadding + (float)i * (chipWidth + gap);
         RectF chip = new RectF(left, chipTop, left + chipWidth, chipBottom);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(55, Color.red(colors[i]), Color.green(colors[i]), Color.blue(colors[i])));
         canvas.drawRoundRect(chip, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(1.1F * this.uiScale);
         this.paint.setColor(Color.argb(135, Color.red(colors[i]), Color.green(colors[i]), Color.blue(colors[i])));
         canvas.drawRoundRect(chip, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         this.drawCenteredFittedText(canvas, labels[i], chip.centerX(), chip.centerY() + 5.0F * this.uiScale, chip.width() - 8.0F * this.uiScale, 15.0F * this.uiScale, Color.rgb(238, 240, 232));
      }

   }

   private void drawCommandMap(Canvas canvas, RectF preview) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(78, 0, 0, 0));
      canvas.drawRoundRect(new RectF(preview.left + 12.0F * this.uiScale, preview.top + 14.0F * this.uiScale, preview.right + 12.0F * this.uiScale, preview.bottom + 14.0F * this.uiScale), 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(19, 28, 34));
      canvas.drawRoundRect(preview, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F * this.uiScale);
      this.paint.setColor(Color.argb(120, 104, 196, 202));
      canvas.drawRoundRect(preview, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      float pad = 28.0F * this.uiScale;
      RectF map = new RectF(preview.left + pad, preview.top + pad, preview.right - pad, preview.bottom - pad);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(198, 190, 170));
      canvas.drawRoundRect(map, 7.0F * this.uiScale, 7.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(76, 65, 56));

      for(int i = 0; i < 5; ++i) {
         float x = map.left + map.width() * (0.16F + (float)i * 0.17F);
         canvas.drawRoundRect(new RectF(x, map.top + map.height() * 0.14F, x + 18.0F * this.uiScale, map.bottom - map.height() * 0.13F), 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      }

      this.paint.setColor(Color.rgb(137, 42, 56));
      canvas.drawRoundRect(new RectF(map.left + map.width() * 0.14F, map.centerY() - 24.0F * this.uiScale, map.right - map.width() * 0.14F, map.centerY() + 24.0F * this.uiScale), 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(95, 224, 70, 58));
      Path cone = new Path();
      cone.moveTo(map.left + map.width() * 0.18F, map.bottom - map.height() * 0.22F);
      cone.lineTo(map.left + map.width() * 0.52F, map.centerY() - 20.0F * this.uiScale);
      cone.lineTo(map.left + map.width() * 0.5F, map.centerY() + 58.0F * this.uiScale);
      cone.close();
      canvas.drawPath(cone, this.paint);
      this.paint.setColor(Color.argb(100, 104, 196, 202));
      canvas.drawCircle(map.right - map.width() * 0.22F, map.top + map.height() * 0.24F, 70.0F * this.uiScale, this.paint);
      this.drawPreviewExhibit(canvas, map.centerX() - 76.0F * this.uiScale, map.top + 74.0F * this.uiScale, 0);
      this.drawPreviewExhibit(canvas, map.centerX() + 18.0F * this.uiScale, map.top + 62.0F * this.uiScale, 1);
      this.drawPreviewExhibit(canvas, map.centerX() + 106.0F * this.uiScale, map.top + 86.0F * this.uiScale, 2);
      this.drawCoinSymbol(canvas, map.left + map.width() * 0.72F, map.bottom - map.height() * 0.2F, 12.0F * this.uiScale);
      this.drawDisruptorSymbol(canvas, map.right - 70.0F * this.uiScale, map.centerY() + 70.0F * this.uiScale, 18.0F * this.uiScale, Color.rgb(104, 196, 202));
      this.paint.setColor(Color.rgb(38, 111, 190));
      canvas.drawCircle(map.left + map.width() * 0.18F, map.bottom - map.height() * 0.23F, 17.0F * this.uiScale, this.paint);
      this.drawLeftFittedText(canvas, "夜间行动桌", preview.left + 24.0F * this.uiScale, preview.top + 24.0F * this.uiScale, preview.width() - 48.0F * this.uiScale, 18.0F * this.uiScale, Color.rgb(238, 240, 232));
   }

   private void drawPreviewMuseum(Canvas canvas, RectF preview) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(70, 0, 0, 0));
      canvas.drawRoundRect(new RectF(preview.left + 10.0F * this.uiScale, preview.top + 12.0F * this.uiScale, preview.right + 10.0F * this.uiScale, preview.bottom + 12.0F * this.uiScale), 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(218, 211, 193));
      canvas.drawRoundRect(preview, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(4.0F * this.uiScale);
      this.paint.setColor(Color.rgb(84, 72, 62));
      canvas.drawRoundRect(preview, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(137, 42, 56));
      RectF carpet = new RectF(preview.left + preview.width() * 0.18F, preview.centerY() - preview.height() * 0.08F, preview.right - preview.width() * 0.18F, preview.centerY() + preview.height() * 0.08F);
      canvas.drawRoundRect(carpet, 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(82, 235, 218, 165));
      canvas.drawRoundRect(new RectF(carpet.left + 12.0F * this.uiScale, carpet.top + 9.0F * this.uiScale, carpet.right - 12.0F * this.uiScale, carpet.bottom - 9.0F * this.uiScale), 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(88, 76, 65));
      float x1 = preview.left + preview.width() * 0.26F;
      float x2 = preview.left + preview.width() * 0.68F;
      canvas.drawRoundRect(new RectF(x1, preview.top + preview.height() * 0.18F, x1 + 38.0F * this.uiScale, preview.bottom - preview.height() * 0.16F), 5.0F * this.uiScale, 5.0F * this.uiScale, this.paint);
      canvas.drawRoundRect(new RectF(x2, preview.top + preview.height() * 0.18F, x2 + 38.0F * this.uiScale, preview.bottom - preview.height() * 0.16F), 5.0F * this.uiScale, 5.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(142, 230, 45, 68));
      Path cone = new Path();
      cone.moveTo(preview.left + preview.width() * 0.23F, preview.top + preview.height() * 0.68F);
      cone.lineTo(preview.left + preview.width() * 0.57F, preview.top + preview.height() * 0.51F);
      cone.lineTo(preview.left + preview.width() * 0.57F, preview.top + preview.height() * 0.82F);
      cone.close();
      canvas.drawPath(cone, this.paint);
      this.paint.setColor(Color.argb(118, 77, 135, 220));
      Path cameraCone = new Path();
      cameraCone.moveTo(preview.right - preview.width() * 0.22F, preview.top + preview.height() * 0.2F);
      cameraCone.lineTo(preview.right - preview.width() * 0.5F, preview.top + preview.height() * 0.42F);
      cameraCone.lineTo(preview.right - preview.width() * 0.32F, preview.top + preview.height() * 0.56F);
      cameraCone.close();
      canvas.drawPath(cameraCone, this.paint);
      this.paint.setColor(Color.rgb(38, 111, 190));
      canvas.drawCircle(preview.left + preview.width() * 0.2F, preview.top + preview.height() * 0.72F, 15.0F * this.uiScale, this.paint);
      this.drawPreviewExhibit(canvas, preview.centerX() - 66.0F * this.uiScale, preview.top + 50.0F * this.uiScale, 0);
      this.drawPreviewExhibit(canvas, preview.centerX() + 4.0F * this.uiScale, preview.top + 44.0F * this.uiScale, 1);
      this.drawPreviewExhibit(canvas, preview.centerX() + 72.0F * this.uiScale, preview.top + 58.0F * this.uiScale, 2);
      this.paint.setColor(Color.rgb(58, 153, 105));
      canvas.drawRoundRect(new RectF(preview.right - 68.0F * this.uiScale, preview.bottom - 54.0F * this.uiScale, preview.right - 20.0F * this.uiScale, preview.bottom - 16.0F * this.uiScale), 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F * this.uiScale);
      this.paint.setColor(Color.rgb(224, 242, 230));
      canvas.drawLine(preview.right - 52.0F * this.uiScale, preview.bottom - 35.0F * this.uiScale, preview.right - 34.0F * this.uiScale, preview.bottom - 35.0F * this.uiScale, this.paint);
      canvas.drawLine(preview.right - 34.0F * this.uiScale, preview.bottom - 35.0F * this.uiScale, preview.right - 42.0F * this.uiScale, preview.bottom - 43.0F * this.uiScale, this.paint);
      canvas.drawLine(preview.right - 34.0F * this.uiScale, preview.bottom - 35.0F * this.uiScale, preview.right - 42.0F * this.uiScale, preview.bottom - 27.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
   }

   private void drawPreviewExhibit(Canvas canvas, float centerX, float centerY, int type) {
      RectF base = new RectF(centerX - 24.0F * this.uiScale, centerY - 16.0F * this.uiScale, centerX + 24.0F * this.uiScale, centerY + 22.0F * this.uiScale);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(166, 157, 139));
      canvas.drawRoundRect(base, 5.0F * this.uiScale, 5.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(74, 160, 218, 232));
      canvas.drawRoundRect(new RectF(base.left + 5.0F * this.uiScale, base.top - 12.0F * this.uiScale, base.right - 5.0F * this.uiScale, base.bottom - 6.0F * this.uiScale), 5.0F * this.uiScale, 5.0F * this.uiScale, this.paint);
      this.paint.setColor(type == 0 ? Color.rgb(213, 168, 79) : (type == 1 ? Color.rgb(91, 136, 154) : Color.rgb(150, 86, 112)));
      if (type == 0) {
         canvas.drawCircle(centerX, centerY - 3.0F * this.uiScale, 10.0F * this.uiScale, this.paint);
      } else if (type == 1) {
         canvas.drawRoundRect(new RectF(centerX - 8.0F * this.uiScale, centerY - 12.0F * this.uiScale, centerX + 8.0F * this.uiScale, centerY + 8.0F * this.uiScale), 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
      } else {
         Path shape = new Path();
         shape.moveTo(centerX, centerY - 14.0F * this.uiScale);
         shape.lineTo(centerX + 13.0F * this.uiScale, centerY + 8.0F * this.uiScale);
         shape.lineTo(centerX - 13.0F * this.uiScale, centerY + 8.0F * this.uiScale);
         shape.close();
         canvas.drawPath(shape, this.paint);
      }

   }

   private void drawTitleProgress(Canvas canvas) {
      int totalStars = 0;
      int perfect = 0;

      for(int stars : this.bestStars) {
         totalStars += stars;
         if (stars >= 3) {
            ++perfect;
         }
      }

      RectF chip = this.titleProgressPanel;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(176, 8, 16, 22));
      canvas.drawRoundRect(chip, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.5F * this.uiScale);
      this.paint.setColor(Color.argb(150, 104, 196, 202));
      canvas.drawRoundRect(chip, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      float left = chip.left + 18.0F * this.uiScale;
      float mid = chip.centerX();
      float top = chip.top + 25.0F * this.uiScale;
      float bottom = chip.top + 52.0F * this.uiScale;
      this.drawStar(canvas, left + 12.0F * this.uiScale, top - 5.0F * this.uiScale, 12.0F * this.uiScale, true, Color.rgb(213, 168, 79));
      this.drawLeftFittedText(canvas, "收藏进度", left + 34.0F * this.uiScale, top, chip.width() * 0.36F, 19.0F * this.uiScale, Color.rgb(238, 240, 232));
      this.drawCenteredFittedText(canvas, totalStars + "/" + this.levelManager.getLevelCount() * 3 + " 星", chip.right - 76.0F * this.uiScale, top, 132.0F * this.uiScale, 19.0F * this.uiScale, Color.rgb(226, 194, 75));
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.1F * this.uiScale);
      this.paint.setColor(Color.argb(72, 104, 196, 202));
      canvas.drawLine(mid, chip.top + 12.0F * this.uiScale, mid, chip.bottom - 12.0F * this.uiScale, this.paint);
      this.drawLeftFittedText(canvas, perfect + "/" + this.levelManager.getLevelCount() + " 间三星点亮", left, bottom, chip.width() * 0.48F, 16.0F * this.uiScale, Color.rgb(220, 226, 228));
      this.drawCenteredFittedText(canvas, this.levelManager.getLevelCount() + " 间展厅", chip.right - 76.0F * this.uiScale, bottom, 132.0F * this.uiScale, 16.0F * this.uiScale, Color.rgb(220, 226, 228));
   }

   private void drawCharacterSelectScreen(Canvas canvas) {
      this.drawScreenBackdrop(canvas, Color.rgb(16, 26, 34), Color.rgb(34, 92, 96));
      this.drawButton(canvas, this.characterBackButton, "返回");
      this.drawCenteredFittedText(canvas, "选择潜行角色", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.135F, (float)this.getWidth() * 0.8F, 44.0F * this.uiScale, Color.rgb(241, 246, 244));
      this.drawCenteredFittedText(canvas, "每位角色拥有独立造型与身份设定 · 点击卡片即可加入本次行动", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.205F, (float)this.getWidth() * 0.78F, 18.0F * this.uiScale, Color.rgb(188, 211, 213));
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(120, 82, 211, 201));
      this.sceneRect.set((float)this.getWidth() * 0.42F, (float)this.getHeight() * 0.235F, (float)this.getWidth() * 0.58F, (float)this.getHeight() * 0.239F);
      canvas.drawRoundRect(this.sceneRect, 2.0F * this.uiScale, 2.0F * this.uiScale, this.paint);
      List<CharacterConfig> characters = CharacterRepository.getAll();

      for(int i = 0; i < this.characterCardButtons.size() && i < characters.size(); ++i) {
         this.drawCharacterCard(canvas, (RectF)this.characterCardButtons.get(i), (CharacterConfig)characters.get(i));
      }

      this.paint.setTextAlign(Align.LEFT);
   }

   private void drawCharacterCard(Canvas canvas, RectF bounds, CharacterConfig character) {
      boolean selected = this.selectedCharacter.getId().equals(character.getId());
      int accent = character.getAccentColor();
      float radius = 12.0F * this.uiScale;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(selected ? 118 : 82, 0, 0, 0));
      this.sceneRect.set(bounds.left + 5.0F * this.uiScale, bounds.top + 7.0F * this.uiScale, bounds.right + 5.0F * this.uiScale, bounds.bottom + 7.0F * this.uiScale);
      canvas.drawRoundRect(this.sceneRect, radius, radius, this.paint);
      this.paint.setColor(selected ? Color.rgb(31, 54, 61) : Color.rgb(27, 40, 49));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      canvas.save();
      canvas.clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom);
      this.paint.setColor(this.withAlpha(accent, selected ? 82 : 42));
      this.sceneRect.set(bounds.left, bounds.top, bounds.right, bounds.top + 8.0F * this.uiScale);
      canvas.drawRect(this.sceneRect, this.paint);
      this.paint.setColor(this.withAlpha(accent, selected ? 38 : 20));
      canvas.drawCircle(bounds.centerX(), bounds.top + bounds.height() * 0.3F, bounds.width() * 0.43F, this.paint);
      this.sceneRect.set(bounds.left + 10.0F * this.uiScale, bounds.top + 14.0F * this.uiScale, bounds.right - 10.0F * this.uiScale, bounds.top + bounds.height() * 0.69F);
      this.paint.setColor(Color.argb(112, 7, 17, 23));
      canvas.drawRoundRect(this.sceneRect, 10.0F * this.uiScale, 10.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.2F * this.uiScale);
      this.paint.setColor(this.withAlpha(accent, 78));
      canvas.drawRoundRect(this.sceneRect, 10.0F * this.uiScale, 10.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(48, 255, 255, 255));
      RectF platform = new RectF(this.sceneRect.centerX() - this.sceneRect.width() * 0.3F, this.sceneRect.bottom - 12.0F * this.uiScale, this.sceneRect.centerX() + this.sceneRect.width() * 0.3F, this.sceneRect.bottom - 3.0F * this.uiScale);
      canvas.drawOval(platform, this.paint);
      this.characterRenderer.drawFullBody(canvas, character, this.sceneRect, this.uiElapsedSeconds, selected);
      this.drawCenteredFittedText(canvas, character.getName(), bounds.centerX(), bounds.top + bounds.height() * 0.735F, bounds.width() - 22.0F * this.uiScale, 25.5F * this.uiScale, Color.rgb(241, 246, 244));
      this.drawCenteredFittedText(canvas, character.getStyleTag(), bounds.centerX(), bounds.top + bounds.height() * 0.79F, bounds.width() - 22.0F * this.uiScale, 17.0F * this.uiScale, accent);
      this.drawWrappedCenteredText(canvas, character.getDescription(), bounds.centerX(), bounds.top + bounds.height() * 0.835F, bounds.width() - 28.0F * this.uiScale, 14.5F * this.uiScale, 19.0F * this.uiScale, 2, Color.rgb(178, 197, 200));
      canvas.restore();
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(selected ? 3.0F * this.uiScale : 1.4F * this.uiScale);
      this.paint.setColor(selected ? accent : Color.argb(132, 90, 119, 128));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      if (selected) {
         float badgeCx = bounds.right - 18.0F * this.uiScale;
         float badgeCy = bounds.top + 19.0F * this.uiScale;
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(accent);
         canvas.drawCircle(badgeCx, badgeCy, 11.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeCap(Cap.ROUND);
         this.paint.setStrokeWidth(2.4F * this.uiScale);
         this.paint.setColor(Color.rgb(9, 28, 31));
         canvas.drawLine(badgeCx - 4.5F * this.uiScale, badgeCy, badgeCx - 1.0F * this.uiScale, badgeCy + 3.7F * this.uiScale, this.paint);
         canvas.drawLine(badgeCx - 1.0F * this.uiScale, badgeCy + 3.7F * this.uiScale, badgeCx + 5.5F * this.uiScale, badgeCy - 4.5F * this.uiScale, this.paint);
         this.paint.setStrokeCap(Cap.BUTT);
         this.paint.setStyle(Style.FILL);
      }

   }

   private void drawLevelSelectScreen(Canvas canvas) {
      this.drawScreenBackdrop(canvas, Color.rgb(16, 26, 34), Color.rgb(36, 84, 90));
      this.drawButton(canvas, this.selectBackButton, "返回");
      this.drawCenteredFittedText(canvas, "选择行动展厅", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.13F, (float)this.getWidth() * 0.82F, 46.0F * this.uiScale, Color.rgb(241, 246, 244));
      this.drawCenteredFittedText(canvas, "v4.1.0 · 全部展厅可自由进入，星级仅记录挑战表现", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.213F, (float)this.getWidth() * 0.82F, 20.0F * this.uiScale, Color.rgb(188, 211, 213));

      for(int i = 0; i < this.levelButtons.size(); ++i) {
         RectF bounds = (RectF)this.levelButtons.get(i);
         Level level = this.levelManager.getLevel(i);
         boolean current = i == this.levelManager.getCurrentLevelNumber() - 1;
         int accent = this.levelAccentColor(i);
         float radius = 11.0F * this.uiScale;
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(82, 0, 0, 0));
         this.sceneRect.set(bounds.left + 4.0F * this.uiScale, bounds.top + 6.0F * this.uiScale, bounds.right + 4.0F * this.uiScale, bounds.bottom + 6.0F * this.uiScale);
         canvas.drawRoundRect(this.sceneRect, radius, radius, this.paint);
         this.paint.setColor(current ? Color.rgb(35, 54, 59) : Color.rgb(27, 40, 49));
         canvas.drawRoundRect(bounds, radius, radius, this.paint);
         canvas.save();
         canvas.clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom);
         this.paint.setColor(this.withAlpha(accent, current ? 82 : 48));
         this.sceneRect.set(bounds.left, bounds.top, bounds.right, bounds.top + 9.0F * this.uiScale);
         canvas.drawRect(this.sceneRect, this.paint);
         this.paint.setColor(this.withAlpha(accent, current ? 34 : 20));
         canvas.drawCircle(bounds.right - 24.0F * this.uiScale, bounds.top + 24.0F * this.uiScale, 58.0F * this.uiScale, this.paint);
         this.drawCenteredFittedText(canvas, "展厅 " + (i + 1) + " · " + this.levelMechanicLabel(i), bounds.centerX(), bounds.top + 35.0F * this.uiScale, bounds.width() - 24.0F * this.uiScale, 19.0F * this.uiScale, accent);
         this.drawCenteredFittedText(canvas, level.getTitle(), bounds.centerX(), bounds.top + 78.0F * this.uiScale, bounds.width() - 22.0F * this.uiScale, 27.0F * this.uiScale, Color.rgb(239, 244, 242));
         this.drawWrappedCenteredText(canvas, level.getObjective(), bounds.centerX(), bounds.top + 99.0F * this.uiScale, bounds.width() - 30.0F * this.uiScale, 13.5F * this.uiScale, 16.5F * this.uiScale, 2, Color.rgb(184, 202, 204));
         this.drawCenteredFittedText(canvas, this.levelSecuritySummary(level), bounds.centerX(), bounds.bottom - 88.0F * this.uiScale, bounds.width() - 26.0F * this.uiScale, 13.0F * this.uiScale, Color.rgb(151, 185, 188));
         this.drawDifficultyDots(canvas, bounds, this.levelSecurityRating(i));
         this.drawBestStars(canvas, bounds, this.bestStars[i]);
         this.drawCenteredFittedText(canvas, current ? "当前预览 · 点击部署" : "开放 · 点击部署", bounds.centerX(), bounds.bottom - 18.0F * this.uiScale, bounds.width() - 24.0F * this.uiScale, 14.0F * this.uiScale, current ? Color.rgb(82, 211, 201) : Color.rgb(207, 218, 219));
         canvas.restore();
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(current ? 2.7F * this.uiScale : 1.5F * this.uiScale);
         this.paint.setColor(current ? accent : Color.argb(148, 89, 119, 128));
         canvas.drawRoundRect(bounds, radius, radius, this.paint);
      }

      this.drawMapConfirmOverlay(canvas);
      this.drawTransitionOverlay(canvas);
      this.paint.setTextAlign(Align.LEFT);
   }

   private int levelSecurityRating(int levelIndex) {
      return Math.min(5, 3 + levelIndex / 3);
   }

   private String levelSecuritySummary(Level level) {
      return Math.round(level.getWorldWidth()) + "×" + Math.round(level.getWorldHeight()) + " · 巡逻" + level.getGuards().size() + " / 监控" + level.getCameras().size() + " / 激光" + level.getLasers().size();
   }

   private void drawDifficultyDots(Canvas canvas, RectF bounds, int difficulty) {
      float dotRadius = 4.2F * this.uiScale;
      float startX = bounds.centerX() - 22.0F * this.uiScale;
      float y = bounds.bottom - 54.0F * this.uiScale;
      this.paint.setStyle(Style.FILL);

      for(int i = 0; i < 5; ++i) {
         this.paint.setColor(i < difficulty ? Color.rgb(151, 54, 54) : Color.argb(90, 40, 45, 50));
         canvas.drawCircle(startX + (float)i * 11.0F * this.uiScale, y, dotRadius, this.paint);
      }

   }

   private int levelAccentColor(int levelIndex) {
      switch (levelIndex % 5) {
         case 1 -> {
            return Color.rgb(224, 70, 58);
         }
         case 2 -> {
            return Color.rgb(226, 194, 75);
         }
         case 3 -> {
            return Color.rgb(77, 135, 220);
         }
         case 4 -> {
            return Color.rgb(58, 153, 105);
         }
         default -> {
            return Color.rgb(104, 196, 202);
         }
      }
   }

   private String levelMechanicLabel(int levelIndex) {
      switch (levelIndex) {
         case 1 -> {
            return "激光";
         }
         case 2 -> {
            return "钥匙门";
         }
         case 3 -> {
            return "监控";
         }
         case 4 -> {
            return "综合";
         }
         case 5 -> {
            return "诱饵";
         }
         case 6 -> {
            return "金库";
         }
         case 7 -> {
            return "迷宫";
         }
         case 8 -> {
            return "控制";
         }
         case 9 -> {
            return "终局";
         }
         default -> {
            return "巡逻";
         }
      }
   }

   private void drawBestStars(Canvas canvas, RectF bounds, int stars) {
      float startX = bounds.centerX() - 27.0F * this.uiScale;
      float y = bounds.bottom - 76.0F * this.uiScale;

      for(int i = 0; i < 3; ++i) {
         this.drawStar(canvas, startX + (float)i * 27.0F * this.uiScale, y, 10.0F * this.uiScale, i < stars, Color.rgb(213, 168, 79));
      }

   }

   private void drawShopScreen(Canvas canvas) {
      this.drawScreenBackdrop(canvas, Color.rgb(16, 26, 34), Color.rgb(52, 82, 66));
      this.drawButton(canvas, this.shopBackButton, "返回");
      this.drawCenteredFittedText(canvas, "夜馆行动补给", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.13F, (float)this.getWidth() * 0.8F, 43.0F * this.uiScale, Color.rgb(241, 246, 244));
      this.drawCenteredFittedText(canvas, "v4.1.0 · 行动资金 " + this.coinsBalance + " · 消耗道具与永久升级", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.205F, (float)this.getWidth() * 0.82F, 18.0F * this.uiScale, Color.rgb(188, 211, 213));
      PowerUp.Type[] types = this.shopTypes();

      for(int i = 0; i < this.shopItemButtons.size() && i < types.length; ++i) {
         this.drawShopItem(canvas, (RectF)this.shopItemButtons.get(i), types[i]);
      }

      UpgradeType[] upgrades = UpgradeType.values();
      int offset = types.length;

      for(int i = 0; i < upgrades.length; ++i) {
         int buttonIndex = offset + i;
         if (buttonIndex < this.shopItemButtons.size()) {
            this.drawUpgradeItem(canvas, (RectF)this.shopItemButtons.get(buttonIndex), upgrades[i]);
         }
      }

   }

   private void drawShopItem(Canvas canvas, RectF bounds, PowerUp.Type type) {
      boolean affordable = this.coinsBalance >= this.shopPrice(type);
      int color = this.powerUpColor(type);
      float radius = 10.0F * this.uiScale;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(76, 0, 0, 0));
      this.sceneRect.set(bounds.left + 4.0F * this.uiScale, bounds.top + 5.0F * this.uiScale, bounds.right + 4.0F * this.uiScale, bounds.bottom + 5.0F * this.uiScale);
      canvas.drawRoundRect(this.sceneRect, radius, radius, this.paint);
      this.paint.setColor(Color.rgb(27, 40, 49));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.paint.setColor(this.withAlpha(color, affordable ? 70 : 30));
      this.sceneRect.set(bounds.left, bounds.top, bounds.left + 7.0F * this.uiScale, bounds.bottom);
      canvas.drawRoundRect(this.sceneRect, 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      float iconX = bounds.left + 48.0F * this.uiScale;
      float iconY = bounds.centerY() - 2.0F * this.uiScale;
      this.paint.setColor(Color.argb(150, 9, 19, 25));
      canvas.drawCircle(iconX, iconY, 31.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F * this.uiScale);
      this.paint.setColor(this.withAlpha(color, affordable ? 230 : 110));
      canvas.drawCircle(iconX, iconY, 29.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.drawPowerUpSymbol(canvas, type, iconX, iconY, 17.0F * this.uiScale, affordable ? color : Color.rgb(115, 128, 133));
      float textLeft = bounds.left + 88.0F * this.uiScale;
      float textWidth = Math.max(80.0F * this.uiScale, bounds.width() - 218.0F * this.uiScale);
      this.drawLeftFittedText(canvas, type.getLabel(), textLeft, bounds.top + 35.0F * this.uiScale, textWidth, 22.0F * this.uiScale, Color.rgb(239, 244, 242));
      this.drawLeftFittedText(canvas, this.shopDescription(type), textLeft, bounds.top + 64.0F * this.uiScale, textWidth, 15.0F * this.uiScale, Color.rgb(171, 191, 194));
      this.drawLeftFittedText(canvas, "库存 " + this.shopStock[type.ordinal()], textLeft, bounds.bottom - 16.0F * this.uiScale, textWidth, 14.0F * this.uiScale, affordable ? Color.rgb(82, 211, 201) : Color.rgb(129, 143, 148));
      RectF price = new RectF(bounds.right - 116.0F * this.uiScale, bounds.centerY() - 24.0F * this.uiScale, bounds.right - 16.0F * this.uiScale, bounds.centerY() + 24.0F * this.uiScale);
      this.paint.setColor(affordable ? Color.rgb(36, 72, 65) : Color.rgb(47, 55, 60));
      canvas.drawRoundRect(price, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.5F * this.uiScale);
      this.paint.setColor(affordable ? Color.rgb(82, 211, 201) : Color.rgb(92, 105, 111));
      canvas.drawRoundRect(price, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.drawCoinSymbol(canvas, price.left + 22.0F * this.uiScale, price.centerY(), 8.5F * this.uiScale);
      this.drawCenteredFittedText(canvas, String.valueOf(this.shopPrice(type)), price.centerX() + 12.0F * this.uiScale, price.centerY() + 6.0F * this.uiScale, price.width() - 36.0F * this.uiScale, 17.0F * this.uiScale, affordable ? -1 : Color.rgb(154, 164, 168));
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.3F * this.uiScale);
      this.paint.setColor(affordable ? this.withAlpha(color, 150) : Color.argb(105, 83, 96, 102));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
   }

   private void drawUpgradeItem(Canvas canvas, RectF bounds, UpgradeType type) {
      int level = this.upgradeLevels[type.ordinal()];
      boolean maxed = level >= 3;
      boolean affordable = !maxed && this.coinsBalance >= this.upgradePrice(type);
      int color = maxed ? Color.rgb(82, 211, 154) : Color.rgb(225, 180, 78);
      float radius = 10.0F * this.uiScale;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(76, 0, 0, 0));
      this.sceneRect.set(bounds.left + 4.0F * this.uiScale, bounds.top + 5.0F * this.uiScale, bounds.right + 4.0F * this.uiScale, bounds.bottom + 5.0F * this.uiScale);
      canvas.drawRoundRect(this.sceneRect, radius, radius, this.paint);
      this.paint.setColor(Color.rgb(30, 41, 48));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.paint.setColor(this.withAlpha(color, !affordable && !maxed ? 30 : 72));
      this.sceneRect.set(bounds.left, bounds.top, bounds.left + 7.0F * this.uiScale, bounds.bottom);
      canvas.drawRoundRect(this.sceneRect, 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      float iconX = bounds.left + 48.0F * this.uiScale;
      float iconY = bounds.centerY() - 2.0F * this.uiScale;
      this.paint.setColor(Color.argb(150, 9, 19, 25));
      canvas.drawCircle(iconX, iconY, 31.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F * this.uiScale);
      this.paint.setColor(this.withAlpha(color, !affordable && !maxed ? 115 : 230));
      canvas.drawCircle(iconX, iconY, 29.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      this.drawCenteredFittedText(canvas, type.getBadge(), iconX, iconY + 7.0F * this.uiScale, 49.0F * this.uiScale, 18.0F * this.uiScale, color);
      float textLeft = bounds.left + 88.0F * this.uiScale;
      float textWidth = Math.max(80.0F * this.uiScale, bounds.width() - 218.0F * this.uiScale);
      this.drawLeftFittedText(canvas, type.getLabel() + "  Lv." + level + "/" + 3, textLeft, bounds.top + 35.0F * this.uiScale, textWidth, 21.0F * this.uiScale, Color.rgb(239, 244, 242));
      this.drawLeftFittedText(canvas, type.getDescription(), textLeft, bounds.top + 64.0F * this.uiScale, textWidth, 15.0F * this.uiScale, Color.rgb(171, 191, 194));
      this.drawLeftFittedText(canvas, maxed ? "已满级 · 永久生效" : "永久升级", textLeft, bounds.bottom - 16.0F * this.uiScale, textWidth, 14.0F * this.uiScale, maxed ? Color.rgb(82, 211, 154) : Color.rgb(225, 180, 78));
      RectF price = new RectF(bounds.right - 116.0F * this.uiScale, bounds.centerY() - 24.0F * this.uiScale, bounds.right - 16.0F * this.uiScale, bounds.centerY() + 24.0F * this.uiScale);
      this.paint.setColor(maxed ? Color.rgb(38, 82, 63) : (affordable ? Color.rgb(78, 63, 34) : Color.rgb(47, 55, 60)));
      canvas.drawRoundRect(price, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.5F * this.uiScale);
      this.paint.setColor(maxed ? Color.rgb(82, 211, 154) : (affordable ? Color.rgb(225, 180, 78) : Color.rgb(92, 105, 111)));
      canvas.drawRoundRect(price, 9.0F * this.uiScale, 9.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      if (maxed) {
         this.drawCenteredFittedText(canvas, "MAX", price.centerX(), price.centerY() + 6.0F * this.uiScale, price.width() - 14.0F * this.uiScale, 17.0F * this.uiScale, -1);
      } else {
         this.drawCoinSymbol(canvas, price.left + 22.0F * this.uiScale, price.centerY(), 8.5F * this.uiScale);
         this.drawCenteredFittedText(canvas, String.valueOf(this.upgradePrice(type)), price.centerX() + 12.0F * this.uiScale, price.centerY() + 6.0F * this.uiScale, price.width() - 36.0F * this.uiScale, 17.0F * this.uiScale, affordable ? -1 : Color.rgb(154, 164, 168));
      }

      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.3F * this.uiScale);
      this.paint.setColor(maxed ? Color.rgb(82, 211, 154) : (affordable ? this.withAlpha(color, 160) : Color.argb(105, 83, 96, 102)));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
   }

   private void drawSettingsScreen(Canvas canvas) {
      this.drawScreenBackdrop(canvas, Color.rgb(16, 26, 34), Color.rgb(42, 80, 77));
      this.drawButton(canvas, this.settingsBackButton, "返回");
      this.drawCenteredFittedText(canvas, "行动设置", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.165F, (float)this.getWidth() * 0.74F, 44.0F * this.uiScale, Color.rgb(241, 246, 244));
      this.drawCenteredFittedText(canvas, "v4.1.0 · 按照你的观察习惯调整辅助显示与反馈", (float)this.getWidth() * 0.5F, (float)this.getHeight() * 0.245F, (float)this.getWidth() * 0.76F, 18.0F * this.uiScale, Color.rgb(188, 211, 213));
      RectF settingsPanel = new RectF(this.routeToggleButton.left - 18.0F * this.uiScale, this.routeToggleButton.top - 18.0F * this.uiScale, this.routeToggleButton.right + 18.0F * this.uiScale, this.progressResetButton.bottom + 18.0F * this.uiScale);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(80, 0, 0, 0));
      this.sceneRect.set(settingsPanel.left + 6.0F * this.uiScale, settingsPanel.top + 8.0F * this.uiScale, settingsPanel.right + 6.0F * this.uiScale, settingsPanel.bottom + 8.0F * this.uiScale);
      canvas.drawRoundRect(this.sceneRect, 14.0F * this.uiScale, 14.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(188, 22, 35, 43));
      canvas.drawRoundRect(settingsPanel, 14.0F * this.uiScale, 14.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.4F * this.uiScale);
      this.paint.setColor(Color.argb(118, 82, 211, 201));
      canvas.drawRoundRect(settingsPanel, 14.0F * this.uiScale, 14.0F * this.uiScale, this.paint);
      this.drawToggle(canvas, this.routeToggleButton, "巡逻轨迹", this.showPatrolRoutes ? "开启" : "关闭", this.showPatrolRoutes);
      this.drawToggle(canvas, this.contrastToggleButton, "警戒区对比", this.highContrastVision ? "高对比" : "标准", this.highContrastVision);
      this.drawToggle(canvas, this.soundToggleButton, "音效反馈", this.soundFeedbackEnabled ? "开启" : "关闭", this.soundFeedbackEnabled);
      this.drawToggle(canvas, this.hapticToggleButton, "震动反馈", this.hapticFeedbackEnabled ? "开启" : "关闭", this.hapticFeedbackEnabled);
      this.drawToggle(canvas, this.reduceMotionToggleButton, "减少动态效果", this.reduceMotion ? "开启" : "关闭", this.reduceMotion);
      this.drawToggle(canvas, this.largeTextToggleButton, "界面大字体", this.largeTextMode ? "开启" : "关闭", this.largeTextMode);
      this.drawButton(canvas, this.progressResetButton, this.progressResetConfirmSeconds > 0.0F ? "确认重置（再次点击）" : "重置星级进度");
      if (this.progressResetConfirmSeconds > 0.0F) {
         this.drawCenteredFittedText(canvas, "仅清除关卡星级；金币、库存和永久升级不受影响。", this.progressResetButton.centerX(), this.progressResetButton.bottom + 27.0F * this.uiScale, this.progressResetButton.width() + 140.0F * this.uiScale, 15.0F * this.uiScale, Color.rgb(225, 180, 78));
      }

   }

   private void drawScreenBackdrop(Canvas canvas, int baseColor, int gridColor) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(baseColor);
      canvas.drawRect(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight(), this.paint);
      this.paint.setColor(Color.argb(72, 28, 47, 57));
      canvas.drawRect(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight() * 0.22F, this.paint);
      this.paint.setColor(Color.argb(46, Color.red(gridColor), Color.green(gridColor), Color.blue(gridColor)));
      canvas.drawCircle((float)this.getWidth() * 0.11F, (float)this.getHeight() * 0.17F, (float)this.getHeight() * 0.34F, this.paint);
      canvas.drawCircle((float)this.getWidth() * 0.91F, (float)this.getHeight() * 0.88F, (float)this.getHeight() * 0.39F, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.2F * this.uiScale);
      this.paint.setColor(Color.argb(42, Color.red(gridColor), Color.green(gridColor), Color.blue(gridColor)));
      float spacing = 86.0F * this.uiScale;

      for(float x = -spacing; x < (float)this.getWidth() + spacing; x += spacing) {
         canvas.drawLine(x, 0.0F, x + (float)this.getHeight() * 0.22F, (float)this.getHeight(), this.paint);
      }

      for(float y = (float)this.getHeight() * 0.1F; y < (float)this.getHeight(); y += spacing) {
         canvas.drawLine(0.0F, y, (float)this.getWidth(), y + (float)this.getWidth() * 0.055F, this.paint);
      }

      this.paint.setStrokeWidth(3.0F * this.uiScale);
      this.paint.setColor(Color.argb(180, 80, 202, 196));
      canvas.drawLine(0.0F, 0.0F, (float)this.getWidth() * 0.32F, 0.0F, this.paint);
      this.paint.setColor(Color.argb(155, 236, 190, 86));
      canvas.drawLine((float)this.getWidth() * 0.32F, 0.0F, (float)this.getWidth() * 0.48F, 0.0F, this.paint);
      this.paint.setStyle(Style.FILL);
   }

   private void drawToggle(Canvas canvas, RectF bounds, String label, String value, boolean active) {
      float radius = 9.0F * this.uiScale;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(28, 43, 52));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.paint.setColor(active ? Color.argb(30, 82, 211, 201) : Color.argb(20, 148, 164, 169));
      this.sceneRect.set(bounds.left, bounds.top, bounds.left + 7.0F * this.uiScale, bounds.bottom);
      canvas.drawRoundRect(this.sceneRect, 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.3F * this.uiScale);
      this.paint.setColor(active ? Color.argb(170, 82, 211, 201) : Color.argb(105, 93, 112, 119));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.drawLeftFittedText(canvas, label, bounds.left + 22.0F * this.uiScale, bounds.centerY() + 7.0F * this.uiScale, bounds.width() * 0.52F, 21.0F * this.uiScale, Color.rgb(235, 242, 240));
      float trackWidth = 66.0F * this.uiScale;
      RectF switchTrack = new RectF(bounds.right - 88.0F * this.uiScale, bounds.centerY() - 15.0F * this.uiScale, bounds.right - 22.0F * this.uiScale, bounds.centerY() + 15.0F * this.uiScale);
      this.drawCenteredFittedText(canvas, value, switchTrack.left - 70.0F * this.uiScale, bounds.centerY() + 6.0F * this.uiScale, 94.0F * this.uiScale, 15.0F * this.uiScale, active ? Color.rgb(82, 211, 201) : Color.rgb(151, 166, 171));
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(active ? Color.rgb(48, 111, 104) : Color.rgb(68, 79, 85));
      canvas.drawRoundRect(switchTrack, 15.0F * this.uiScale, 15.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.2F * this.uiScale);
      this.paint.setColor(active ? Color.rgb(82, 211, 201) : Color.rgb(111, 126, 132));
      canvas.drawRoundRect(switchTrack, 15.0F * this.uiScale, 15.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.FILL);
      float knobX = active ? switchTrack.right - 15.0F * this.uiScale : switchTrack.left + 15.0F * this.uiScale;
      this.paint.setColor(active ? Color.rgb(226, 252, 248) : Color.rgb(180, 190, 193));
      canvas.drawCircle(knobX, switchTrack.centerY(), 11.0F * this.uiScale, this.paint);
   }

   private void drawMuseum(Canvas canvas) {
      Level level = this.getLevel();
      RectF bounds = level.getBounds();
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(10, 13, 18));
      canvas.drawRect(this.cameraX - 80.0F, this.cameraY - 80.0F, this.cameraX + (float)this.screenWidth / this.worldScale + 80.0F, this.cameraY + (float)this.screenHeight / this.worldScale + 80.0F, this.paint);
      this.paint.setColor(Color.argb(70, 0, 0, 0));
      canvas.drawRoundRect(new RectF(bounds.left + 20.0F, bounds.top + 22.0F, bounds.right + 20.0F, bounds.bottom + 22.0F), 12.0F, 12.0F, this.paint);
      int floorBase = this.levelFloorColor(this.levelManager.getCurrentLevelNumber() - 1);
      this.paint.setColor(floorBase);
      canvas.drawRect(bounds, this.paint);
      this.drawAmbientLight(canvas, bounds);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(12.0F);
      this.paint.setColor(this.levelWallDarkColor(this.levelManager.getCurrentLevelNumber() - 1));
      canvas.drawRect(bounds, this.paint);
      this.paint.setStrokeWidth(3.0F);
      this.paint.setColor(this.levelAccentColor(this.levelManager.getCurrentLevelNumber() - 1));
      canvas.drawRect(bounds.left + 11.0F, bounds.top + 11.0F, bounds.right - 11.0F, bounds.bottom - 11.0F, this.paint);
      this.drawFloorGuides(canvas, bounds);
      this.environmentRenderer.draw(canvas, bounds, this.levelManager.getCurrentLevelNumber(), level.getTitle(), this.uiElapsedSeconds, this.reduceMotion, this);
      this.drawDecorativeExhibits(canvas, bounds);
      if (this.showPatrolRoutes) {
         this.drawPatrolRoutes(canvas);
      }

      for(Wall wall : level.getWalls()) {
         this.drawWall(canvas, wall);
      }

      for(Door door : level.getDoors()) {
         this.drawDoor(canvas, door);
      }

      for(Guard guard : level.getGuards()) {
         this.drawGuardVision(canvas, guard);
      }

      for(SecurityCamera camera : level.getCameras()) {
         this.drawCameraVision(canvas, camera);
      }

      for(Laser laser : level.getLasers()) {
         this.drawLaser(canvas, laser);
      }

      for(KeyItem keyItem : level.getKeyItems()) {
         this.drawKeyItem(canvas, keyItem);
      }

      for(Coin coin : this.coins) {
         this.drawCoin(canvas, coin);
      }

      for(PowerUp powerUp : this.powerUps) {
         this.drawPowerUp(canvas, powerUp);
      }

      this.drawDisruptors(canvas);
      this.drawDecoy(canvas);
      this.drawDecoyCoinOverlay(canvas);
      this.drawTreasure(canvas);
      this.drawExit(canvas);

      for(Guard guard : level.getGuards()) {
         if (guard.getY() <= this.player.getY()) {
            this.drawGuard(canvas, guard);
         }
      }

      for(SecurityCamera camera : level.getCameras()) {
         if (camera.getY() <= this.player.getY()) {
            this.drawCamera(canvas, camera);
         }
      }

      this.drawPlayer(canvas);
      this.drawForegroundOccluders(canvas, level.getBounds());

      for(Guard guard : level.getGuards()) {
         if (guard.getY() > this.player.getY()) {
            this.drawGuard(canvas, guard);
         }
      }

      for(SecurityCamera camera : level.getCameras()) {
         if (camera.getY() > this.player.getY()) {
            this.drawCamera(canvas, camera);
         }
      }

      this.floatingText.draw(canvas);
   }

   private void drawFloorGuides(Canvas canvas, RectF bounds) {
      int levelIndex = this.levelManager.getCurrentLevelNumber() - 1;
      int accent = this.levelAccentColor(levelIndex);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(34, 255, 255, 255));
      canvas.drawRect(bounds.left + 18.0F, bounds.top + 18.0F, bounds.right - 18.0F, bounds.top + 46.0F, this.paint);
      this.paint.setColor(Color.argb(32, 0, 0, 0));
      canvas.drawRect(bounds.left + 20.0F, bounds.bottom - 52.0F, bounds.right - 20.0F, bounds.bottom - 18.0F, this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(this.levelCarpetColor(levelIndex));
      RectF carpet = new RectF(bounds.left + bounds.width() * 0.12F, bounds.centerY() - 42.0F, bounds.right - bounds.width() * 0.12F, bounds.centerY() + 42.0F);
      canvas.drawRoundRect(carpet, 8.0F, 8.0F, this.paint);
      this.paint.setColor(Color.argb(62, 235, 218, 165));
      canvas.drawRoundRect(new RectF(carpet.left + 12.0F, carpet.top + 12.0F, carpet.right - 12.0F, carpet.bottom - 12.0F), 5.0F, 5.0F, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.6F);
      this.paint.setColor(Color.argb(54, Color.red(accent), Color.green(accent), Color.blue(accent)));
      float spacing = levelIndex == 2 ? 72.0F : (levelIndex == 3 ? 104.0F : 82.0F);

      for(float x = bounds.left + 70.0F; x < bounds.right; x += spacing) {
         canvas.drawLine(x, bounds.top + 10.0F, x, bounds.bottom - 10.0F, this.paint);
      }

      for(float y = bounds.top + 70.0F; y < bounds.bottom; y += spacing) {
         canvas.drawLine(bounds.left + 10.0F, y, bounds.right - 10.0F, y, this.paint);
      }

      this.paint.setStrokeWidth(1.0F);
      this.paint.setColor(Color.argb(34, 30, 34, 38));
      float tile = spacing * 0.52F;

      for(float x = bounds.left + 42.0F; x < bounds.right - 20.0F; x += tile) {
         canvas.drawLine(x, bounds.top + 40.0F, x, bounds.bottom - 40.0F, this.paint);
      }

      for(float y = bounds.top + 42.0F; y < bounds.bottom - 20.0F; y += tile) {
         canvas.drawLine(bounds.left + 40.0F, y, bounds.right - 40.0F, y, this.paint);
      }

      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(26, 84, 70, 54));

      for(int i = 0; i < 16; ++i) {
         float x = bounds.left + 80.0F + (float)i * 173.0F % Math.max(1.0F, bounds.width() - 160.0F);
         float y = bounds.top + 70.0F + (float)i * 119.0F % Math.max(1.0F, bounds.height() - 140.0F);
         canvas.drawOval(new RectF(x - 10.0F, y - 4.0F, x + 12.0F, y + 5.0F), this.paint);
      }

      this.paint.setColor(Color.argb(55, 255, 255, 255));

      for(float x = bounds.left + 42.0F; x < bounds.right; x += 164.0F) {
         canvas.drawLine(x, bounds.top + 18.0F, x + 48.0F, bounds.top + 18.0F, this.paint);
         canvas.drawLine(x, bounds.bottom - 18.0F, x + 48.0F, bounds.bottom - 18.0F, this.paint);
      }

      this.paint.setStrokeWidth(2.2F);
      this.paint.setColor(Color.argb(70, Color.red(accent), Color.green(accent), Color.blue(accent)));
      canvas.drawLine(bounds.left + 34.0F, bounds.top + 34.0F, bounds.right - 34.0F, bounds.top + 34.0F, this.paint);
      canvas.drawLine(bounds.left + 34.0F, bounds.bottom - 34.0F, bounds.right - 34.0F, bounds.bottom - 34.0F, this.paint);
      this.paint.setColor(Color.argb(34, 226, 194, 75));
      canvas.drawLine(bounds.left + 34.0F, bounds.centerY(), bounds.right - 34.0F, bounds.centerY(), this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(38, Color.red(accent), Color.green(accent), Color.blue(accent)));
      canvas.drawCircle(bounds.centerX(), bounds.centerY(), Math.min(bounds.width(), bounds.height()) * 0.18F, this.paint);
   }

   private void drawAmbientLight(Canvas canvas, RectF bounds) {
      int accent = this.levelAccentColor(this.levelManager.getCurrentLevelNumber() - 1);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(28, Color.red(accent), Color.green(accent), Color.blue(accent)));
      canvas.drawOval(new RectF(bounds.left + bounds.width() * 0.16F, bounds.top + 42.0F, bounds.left + bounds.width() * 0.48F, bounds.top + bounds.height() * 0.28F), this.paint);
      this.paint.setColor(Color.argb(24, 255, 245, 200));
      canvas.drawOval(new RectF(bounds.right - bounds.width() * 0.42F, bounds.top + 36.0F, bounds.right - bounds.width() * 0.12F, bounds.top + bounds.height() * 0.3F), this.paint);
      this.paint.setColor(Color.argb(18, 0, 0, 0));
      canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + 38.0F, this.paint);
      canvas.drawRect(bounds.left, bounds.bottom - 40.0F, bounds.right, bounds.bottom, this.paint);
   }

   private int levelFloorColor(int levelIndex) {
      switch (levelIndex % 5) {
         case 1 -> {
            return Color.rgb(191, 196, 197);
         }
         case 2 -> {
            return Color.rgb(183, 180, 169);
         }
         case 3 -> {
            return Color.rgb(198, 205, 210);
         }
         case 4 -> {
            return Color.rgb(187, 201, 190);
         }
         default -> {
            return Color.rgb(214, 207, 188);
         }
      }
   }

   private int levelCarpetColor(int levelIndex) {
      switch (levelIndex % 5) {
         case 1 -> {
            return Color.rgb(114, 39, 51);
         }
         case 2 -> {
            return Color.rgb(94, 92, 96);
         }
         case 3 -> {
            return Color.rgb(54, 86, 123);
         }
         case 4 -> {
            return Color.rgb(42, 105, 82);
         }
         default -> {
            return Color.rgb(142, 45, 59);
         }
      }
   }

   private int levelWallDarkColor(int levelIndex) {
      switch (levelIndex % 5) {
         case 1 -> {
            return Color.rgb(69, 65, 70);
         }
         case 2 -> {
            return Color.rgb(62, 67, 73);
         }
         case 3 -> {
            return Color.rgb(58, 67, 78);
         }
         case 4 -> {
            return Color.rgb(50, 72, 65);
         }
         default -> {
            return Color.rgb(76, 65, 56);
         }
      }
   }

   private void drawDecorativeExhibits(Canvas canvas, RectF bounds) {
      for(float x = bounds.left + 190.0F; x < bounds.right - 130.0F; x += 270.0F) {
         this.drawDisplayCase(canvas, x, bounds.top + 100.0F, 84.0F, 48.0F, Color.rgb(81, 126, 147));
         this.drawDisplayCase(canvas, x + 42.0F, bounds.bottom - 146.0F, 84.0F, 48.0F, Color.rgb(144, 100, 62));
      }

      this.drawPillar(canvas, bounds.left + 96.0F, bounds.top + 96.0F);
      this.drawPillar(canvas, bounds.right - 96.0F, bounds.top + 96.0F);
      this.drawPillar(canvas, bounds.left + 96.0F, bounds.bottom - 96.0F);
      this.drawPillar(canvas, bounds.right - 96.0F, bounds.bottom - 96.0F);
      if (bounds.width() > 1300.0F) {
         this.drawPillar(canvas, bounds.centerX() - 250.0F, bounds.centerY() - 220.0F);
         this.drawPillar(canvas, bounds.centerX() + 250.0F, bounds.centerY() + 220.0F);
      }

   }

   private void drawForegroundOccluders(Canvas canvas, RectF bounds) {
      for(float x = bounds.left + 190.0F; x < bounds.right - 130.0F; x += 270.0F) {
         this.drawDisplayCaseOccluderIfNeeded(canvas, x, bounds.top + 100.0F, 84.0F, 48.0F);
         this.drawDisplayCaseOccluderIfNeeded(canvas, x + 42.0F, bounds.bottom - 146.0F, 84.0F, 48.0F);
      }

      this.drawPillarOccluderIfNeeded(canvas, bounds.left + 96.0F, bounds.top + 96.0F);
      this.drawPillarOccluderIfNeeded(canvas, bounds.right - 96.0F, bounds.top + 96.0F);
      this.drawPillarOccluderIfNeeded(canvas, bounds.left + 96.0F, bounds.bottom - 96.0F);
      this.drawPillarOccluderIfNeeded(canvas, bounds.right - 96.0F, bounds.bottom - 96.0F);
      if (bounds.width() > 1300.0F) {
         this.drawPillarOccluderIfNeeded(canvas, bounds.centerX() - 250.0F, bounds.centerY() - 220.0F);
         this.drawPillarOccluderIfNeeded(canvas, bounds.centerX() + 250.0F, bounds.centerY() + 220.0F);
      }

   }

   private void drawDisplayCaseOccluderIfNeeded(Canvas canvas, float centerX, float centerY, float width, float height) {
      if (!(this.player.getY() <= centerY - height * 0.08F) && !(Math.abs(this.player.getX() - centerX) > width * 0.88F) && !(this.player.getY() > centerY + height * 0.82F)) {
         RectF lip = new RectF(centerX - width * 0.46F, centerY - height * 0.34F, centerX + width * 0.46F, centerY + height * 0.18F);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(138, 174, 165, 144));
         canvas.drawRoundRect(lip, 7.0F, 7.0F, this.paint);
         this.paint.setColor(Color.argb(70, 160, 218, 232));
         canvas.drawRoundRect(new RectF(lip.left + 8.0F, lip.top - 14.0F, lip.right - 8.0F, lip.top + 10.0F), 7.0F, 7.0F, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(1.8F);
         this.paint.setColor(Color.argb(115, 255, 255, 255));
         canvas.drawLine(lip.left + 10.0F, lip.top - 9.0F, lip.right - 14.0F, lip.top - 9.0F, this.paint);
      }
   }

   private void drawPillarOccluderIfNeeded(Canvas canvas, float centerX, float centerY) {
      if (!(this.player.getY() <= centerY - 8.0F) && !(Math.abs(this.player.getX() - centerX) > 34.0F) && !(this.player.getY() > centerY + 56.0F)) {
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(152, 171, 161, 141));
         canvas.drawCircle(centerX, centerY, 27.0F, this.paint);
         this.paint.setColor(Color.argb(130, 110, 96, 80));
         canvas.drawCircle(centerX, centerY, 18.0F, this.paint);
         this.paint.setColor(Color.argb(120, 205, 197, 178));
         canvas.drawCircle(centerX - 6.0F, centerY - 7.0F, 7.0F, this.paint);
      }
   }

   private void drawDisplayCase(Canvas canvas, float centerX, float centerY, float width, float height, int accentColor) {
      float left = centerX - width * 0.5F;
      float top = centerY - height * 0.5F;
      float right = centerX + width * 0.5F;
      float bottom = centerY + height * 0.5F;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(52, 0, 0, 0));
      this.sceneRect.set(left + 9.0F, top + 10.0F, right + 9.0F, bottom + 10.0F);
      canvas.drawRoundRect(this.sceneRect, 8.0F, 8.0F, this.paint);
      this.paint.setColor(Color.rgb(104, 94, 80));
      this.sceneRect.set(left, top, right, bottom);
      canvas.drawRoundRect(this.sceneRect, 8.0F, 8.0F, this.paint);
      this.paint.setColor(Color.rgb(181, 171, 149));
      this.sceneRect.set(left + 3.0F, top + 3.0F, right - 3.0F, bottom - height * 0.34F);
      canvas.drawRoundRect(this.sceneRect, 6.0F, 6.0F, this.paint);
      this.paint.setColor(Color.argb(70, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));
      this.sceneRect.set(left + 9.0F, top + 9.0F, right - 9.0F, bottom - 8.0F);
      canvas.drawRoundRect(this.sceneRect, 5.0F, 5.0F, this.paint);
      this.paint.setColor(Color.argb(38, 0, 0, 0));
      this.sceneRect.set(left + 8.0F, bottom - height * 0.3F, right - 8.0F, bottom - 6.0F);
      canvas.drawRoundRect(this.sceneRect, 4.0F, 4.0F, this.paint);
      float glassTop = top - Math.min(22.0F, height * 0.42F);
      this.paint.setColor(Color.argb(48, 102, 190, 216));
      this.sceneRect.set(left + 13.0F, glassTop, right - 13.0F, top + 20.0F);
      canvas.drawRoundRect(this.sceneRect, 8.0F, 8.0F, this.paint);
      this.paint.setColor(Color.argb(38, 255, 255, 255));
      this.scenePath.reset();
      this.scenePath.moveTo(left + 19.0F, glassTop + 5.0F);
      this.scenePath.lineTo(right - 32.0F, glassTop + 5.0F);
      this.scenePath.lineTo(right - 45.0F, top + 14.0F);
      this.scenePath.lineTo(left + 23.0F, top + 10.0F);
      this.scenePath.close();
      canvas.drawPath(this.scenePath, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F);
      this.paint.setColor(Color.argb(145, 220, 244, 248));
      this.sceneRect.set(left + 13.0F, glassTop, right - 13.0F, top + 20.0F);
      canvas.drawRoundRect(this.sceneRect, 8.0F, 8.0F, this.paint);
      this.paint.setColor(Color.argb(115, 255, 255, 255));
      canvas.drawLine(left + 20.0F, glassTop + 7.0F, right - 31.0F, glassTop + 7.0F, this.paint);
      this.paint.setStyle(Style.FILL);
   }

   private void drawPillar(Canvas canvas, float centerX, float centerY) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(52, 0, 0, 0));
      this.sceneRect.set(centerX - 29.0F + 8.0F, centerY - 22.0F + 9.0F, centerX + 29.0F + 8.0F, centerY + 31.0F + 9.0F);
      canvas.drawOval(this.sceneRect, this.paint);
      this.paint.setColor(Color.rgb(93, 82, 70));
      canvas.drawCircle(centerX, centerY, 29.0F, this.paint);
      this.paint.setColor(Color.rgb(176, 166, 145));
      canvas.drawCircle(centerX, centerY, 25.0F, this.paint);
      this.paint.setColor(Color.rgb(116, 101, 83));
      canvas.drawCircle(centerX, centerY, 18.0F, this.paint);
      this.paint.setColor(Color.rgb(198, 190, 171));
      canvas.drawCircle(centerX - 5.0F, centerY - 6.0F, 11.0F, this.paint);
      this.paint.setColor(Color.argb(90, 255, 255, 255));
      this.sceneRect.set(centerX - 12.0F, centerY - 13.0F, centerX + 3.0F, centerY + 2.0F);
      canvas.drawArc(this.sceneRect, 200.0F, 112.0F, true, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F);
      this.paint.setColor(Color.argb(105, 230, 220, 195));
      canvas.drawCircle(centerX, centerY, 24.0F, this.paint);
      this.paint.setStrokeWidth(1.0F);
      this.paint.setColor(Color.argb(76, 63, 52, 43));

      for(int i = 0; i < 6; ++i) {
         float angle = (float)((Math.PI * 2D) * (double)i / (double)6.0F);
         canvas.drawLine(centerX + (float)Math.cos((double)angle) * 19.0F, centerY + (float)Math.sin((double)angle) * 19.0F, centerX + (float)Math.cos((double)angle) * 25.0F, centerY + (float)Math.sin((double)angle) * 25.0F, this.paint);
      }

      this.paint.setStyle(Style.FILL);
   }

   private void drawPatrolRoutes(Canvas canvas) {
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(3.0F);
      this.paint.setColor(Color.argb(74, 230, 236, 238));
      this.paint.setPathEffect(new DashPathEffect(new float[]{14.0F, 12.0F}, 0.0F));

      for(Guard guard : this.getLevel().getGuards()) {
         List<PointF> points = guard.getPatrolPath().getPoints();
         if (points.size() >= 2) {
            Path path = new Path();
            PointF first = (PointF)points.get(0);
            path.moveTo(first.x, first.y);

            for(int i = 1; i < points.size(); ++i) {
               PointF point = (PointF)points.get(i);
               path.lineTo(point.x, point.y);
            }

            path.close();
            canvas.drawPath(path, this.paint);
         }
      }

      this.paint.setPathEffect((PathEffect)null);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(110, 230, 236, 238));

      for(Guard guard : this.getLevel().getGuards()) {
         for(PointF point : guard.getPatrolPath().getPoints()) {
            canvas.drawCircle(point.x, point.y, 5.0F, this.paint);
         }
      }

   }

   private void drawWall(Canvas canvas, Wall wall) {
      RectF wallBounds = wall.getBounds();
      float cap = Math.min(12.0F, Math.min(wallBounds.width(), wallBounds.height()) * 0.32F);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(76, 0, 0, 0));
      this.sceneRect.set(wallBounds.left + 9.0F, wallBounds.top + 12.0F, wallBounds.right + 9.0F, wallBounds.bottom + 12.0F);
      canvas.drawRoundRect(this.sceneRect, 7.0F, 7.0F, this.paint);
      this.paint.setColor(Color.rgb(68, 59, 53));
      canvas.drawRoundRect(wallBounds, 6.0F, 6.0F, this.paint);
      this.paint.setColor(Color.rgb(104, 89, 75));
      this.sceneRect.set(wallBounds.left + 3.0F, wallBounds.top + 3.0F, wallBounds.right - 3.0F, wallBounds.bottom - 3.0F);
      canvas.drawRoundRect(this.sceneRect, 5.0F, 5.0F, this.paint);
      this.paint.setColor(Color.rgb(139, 120, 96));
      this.sceneRect.set(wallBounds.left + 3.0F, wallBounds.top + 3.0F, wallBounds.right - 3.0F, wallBounds.top + cap);
      canvas.drawRoundRect(this.sceneRect, 4.0F, 4.0F, this.paint);
      this.paint.setColor(Color.rgb(57, 50, 46));
      this.sceneRect.set(wallBounds.right - cap, wallBounds.top + 5.0F, wallBounds.right - 3.0F, wallBounds.bottom - 3.0F);
      canvas.drawRoundRect(this.sceneRect, 3.0F, 3.0F, this.paint);
      this.paint.setColor(Color.argb(48, 0, 0, 0));
      canvas.drawRect(wallBounds.left + 5.0F, wallBounds.bottom - cap * 0.72F, wallBounds.right - 4.0F, wallBounds.bottom - 3.0F, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.5F);
      this.paint.setColor(Color.argb(105, 224, 210, 184));
      canvas.drawLine(wallBounds.left + 7.0F, wallBounds.top + 5.0F, wallBounds.right - 8.0F, wallBounds.top + 5.0F, this.paint);
      this.paint.setStrokeWidth(1.0F);
      this.paint.setColor(Color.argb(64, 38, 30, 24));
      if (wallBounds.width() >= wallBounds.height()) {
         for(float x = wallBounds.left + 22.0F; x < wallBounds.right - 12.0F; x += 46.0F) {
            canvas.drawLine(x, wallBounds.top + cap, x + 18.0F, wallBounds.top + cap, this.paint);
         }
      } else {
         for(float y = wallBounds.top + 22.0F; y < wallBounds.bottom - 12.0F; y += 46.0F) {
            canvas.drawLine(wallBounds.left + cap, y, wallBounds.left + cap, y + 18.0F, this.paint);
         }
      }

      this.paint.setStyle(Style.FILL);
   }

   private void drawDoor(Canvas canvas, Door door) {
      RectF bounds = door.getBounds();
      int color = door.getColor();
      boolean hasKey = this.state.hasKey(door.getKeyCode());
      boolean open = door.isOpen();
      boolean vertical = bounds.height() >= bounds.width();
      float inset = Math.min(7.0F, Math.min(bounds.width(), bounds.height()) * 0.16F);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(74, 0, 0, 0));
      this.sceneRect.set(bounds.left + 7.0F, bounds.top + 8.0F, bounds.right + 7.0F, bounds.bottom + 8.0F);
      canvas.drawRoundRect(this.sceneRect, 8.0F, 8.0F, this.paint);
      this.paint.setColor(open ? Color.rgb(45, 78, 64) : Color.rgb(42, 47, 53));
      this.sceneRect.set(bounds.left - 4.0F, bounds.top - 4.0F, bounds.right + 4.0F, bounds.bottom + 4.0F);
      canvas.drawRoundRect(this.sceneRect, 9.0F, 9.0F, this.paint);
      this.paint.setColor(open ? Color.argb(110, 58, 153, 105) : Color.argb(235, Math.max(32, Color.red(color) - 36), Math.max(32, Color.green(color) - 40), Math.max(32, Color.blue(color) - 44)));
      canvas.drawRoundRect(bounds, 7.0F, 7.0F, this.paint);
      this.paint.setColor(open ? Color.argb(74, 180, 230, 196) : Color.argb(66, 255, 255, 255));
      this.sceneRect.set(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset);
      canvas.drawRoundRect(this.sceneRect, 5.0F, 5.0F, this.paint);
      this.paint.setColor(Color.argb(open ? 46 : 68, 0, 0, 0));
      if (vertical) {
         this.sceneRect.set(bounds.centerX() + 2.0F, bounds.top + inset, bounds.right - inset, bounds.bottom - inset);
      } else {
         this.sceneRect.set(bounds.left + inset, bounds.centerY() + 2.0F, bounds.right - inset, bounds.bottom - inset);
      }

      canvas.drawRoundRect(this.sceneRect, 4.0F, 4.0F, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(3.0F);
      this.paint.setColor(open ? Color.rgb(144, 214, 176) : color);
      canvas.drawRoundRect(bounds, 7.0F, 7.0F, this.paint);
      this.paint.setStrokeWidth(1.5F);
      this.paint.setColor(Color.argb(115, 255, 255, 255));
      if (vertical) {
         canvas.drawLine(bounds.left + inset + 2.0F, bounds.top + inset, bounds.left + inset + 2.0F, bounds.bottom - inset, this.paint);
      } else {
         canvas.drawLine(bounds.left + inset, bounds.top + inset + 2.0F, bounds.right - inset, bounds.top + inset + 2.0F, this.paint);
      }

      this.paint.setStyle(Style.FILL);
      float lockX = vertical ? bounds.centerX() : bounds.centerX() + bounds.width() * 0.26F;
      float lockY = vertical ? bounds.centerY() + bounds.height() * 0.23F : bounds.centerY();
      this.paint.setColor(Color.rgb(28, 34, 40));
      this.sceneRect.set(lockX - 13.0F, lockY - 10.0F, lockX + 13.0F, lockY + 10.0F);
      canvas.drawRoundRect(this.sceneRect, 4.0F, 4.0F, this.paint);
      this.paint.setColor(!open && !hasKey ? color : Color.rgb(91, 205, 139));
      canvas.drawCircle(lockX - 5.0F, lockY, 3.5F, this.paint);
      this.paint.setColor(Color.rgb(170, 181, 186));
      canvas.drawCircle(lockX + 5.0F, lockY, 2.5F, this.paint);
      if (this.isPlayerNearDoor(door)) {
         this.drawWorldCenteredText(canvas, hasKey ? (open ? "可关闭" : "可开启") : door.getLabel(), bounds.centerX(), bounds.top - 12.0F, bounds.width() + 80.0F, 16.0F, hasKey ? Color.rgb(245, 247, 241) : color);
      }

   }

   private void drawLaser(Canvas canvas, Laser laser) {
      boolean active = laser.isActive(this.levelElapsedSeconds);
      float pulse = 0.5F + 0.5F * (float)Math.sin((double)(this.levelElapsedSeconds * 12.0F + laser.getStartX() * 0.02F));
      int beamAlpha = active ? 226 : 58;
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeCap(Cap.ROUND);
      this.paint.setStrokeWidth(active ? 24.0F + pulse * 4.0F : 12.0F);
      this.paint.setColor(Color.argb(active ? 54 : 25, 235, 54, 76));
      canvas.drawLine(laser.getStartX(), laser.getStartY(), laser.getEndX(), laser.getEndY(), this.paint);
      this.paint.setStrokeWidth(active ? 9.0F : 5.0F);
      this.paint.setColor(Color.argb(beamAlpha, 230, 45, 68));
      canvas.drawLine(laser.getStartX(), laser.getStartY(), laser.getEndX(), laser.getEndY(), this.paint);
      this.paint.setStrokeWidth(active ? 2.8F : 1.5F);
      this.paint.setColor(Color.argb(active ? 245 : 95, 255, 224, 228));
      canvas.drawLine(laser.getStartX(), laser.getStartY(), laser.getEndX(), laser.getEndY(), this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(70, 0, 0, 0));
      canvas.drawCircle(laser.getStartX() + 3.0F, laser.getStartY() + 4.0F, 16.0F, this.paint);
      canvas.drawCircle(laser.getEndX() + 3.0F, laser.getEndY() + 4.0F, 16.0F, this.paint);
      this.paint.setColor(Color.rgb(52, 43, 48));
      canvas.drawCircle(laser.getStartX(), laser.getStartY(), 15.0F, this.paint);
      canvas.drawCircle(laser.getEndX(), laser.getEndY(), 15.0F, this.paint);
      this.paint.setColor(active ? Color.rgb(166, 40, 58) : Color.rgb(94, 70, 77));
      canvas.drawCircle(laser.getStartX(), laser.getStartY(), 10.0F, this.paint);
      canvas.drawCircle(laser.getEndX(), laser.getEndY(), 10.0F, this.paint);
      this.paint.setColor(active ? Color.rgb(255, 105, 120) : Color.rgb(126, 93, 99));
      canvas.drawCircle(laser.getStartX(), laser.getStartY(), 4.0F + pulse, this.paint);
      canvas.drawCircle(laser.getEndX(), laser.getEndY(), 4.0F + pulse, this.paint);
      this.paint.setColor(Color.argb(active ? 210 : 80, 255, 255, 255));
      canvas.drawCircle(laser.getStartX() - 2.0F, laser.getStartY() - 2.0F, 1.6F, this.paint);
      canvas.drawCircle(laser.getEndX() - 2.0F, laser.getEndY() - 2.0F, 1.6F, this.paint);
      this.paint.setStrokeCap(Cap.BUTT);
   }

   private void drawKeyItem(Canvas canvas, KeyItem keyItem) {
      if (!keyItem.isCollected()) {
         RectF bounds = keyItem.getBounds();
         float bob = (float)Math.sin((double)(this.levelElapsedSeconds * 3.8F + bounds.centerX() * 0.02F)) * 4.0F;
         float cx = bounds.centerX();
         float cy = bounds.centerY() + bob;
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(56, 0, 0, 0));
         this.sceneRect.set(cx - bounds.width() * 0.42F, bounds.bottom + 4.0F, cx + bounds.width() * 0.42F, bounds.bottom + 14.0F);
         canvas.drawOval(this.sceneRect, this.paint);
         this.paint.setColor(Color.argb(48, Color.red(keyItem.getColor()), Color.green(keyItem.getColor()), Color.blue(keyItem.getColor())));
         canvas.drawCircle(cx, cy, 35.0F, this.paint);
         this.paint.setColor(Color.rgb(43, 48, 54));
         this.sceneRect.set(cx - bounds.width() * 0.48F, cy - bounds.height() * 0.36F, cx + bounds.width() * 0.48F, cy + bounds.height() * 0.36F);
         canvas.drawRoundRect(this.sceneRect, 9.0F, 9.0F, this.paint);
         this.paint.setColor(keyItem.getColor());
         this.sceneRect.set(cx - bounds.width() * 0.42F, cy - bounds.height() * 0.3F, cx + bounds.width() * 0.42F, cy + bounds.height() * 0.3F);
         canvas.drawRoundRect(this.sceneRect, 7.0F, 7.0F, this.paint);
         this.paint.setColor(Color.argb(64, 255, 255, 255));
         this.sceneRect.set(cx - bounds.width() * 0.32F, cy - bounds.height() * 0.24F, cx + bounds.width() * 0.1F, cy - bounds.height() * 0.04F);
         canvas.drawRoundRect(this.sceneRect, 4.0F, 4.0F, this.paint);
         this.iconRenderer.drawKey(canvas, cx, cy, bounds.width() * 0.25F, Color.rgb(255, 231, 134));
         this.drawWorldCenteredText(canvas, keyItem.getLabel(), cx, bounds.bottom + 25.0F, 100.0F, 16.0F, Color.rgb(70, 55, 24));
      }
   }

   private void drawKeySymbol(Canvas canvas, float cx, float cy, float size, int color) {
      this.iconRenderer.drawKey(canvas, cx, cy, size, color);
   }

   private void drawCoinSymbol(Canvas canvas, float cx, float cy, float radius) {
      this.iconRenderer.drawCoin(canvas, cx, cy, radius, this.levelElapsedSeconds * 5.0F + cx * 0.03F, false, false);
   }

   private void drawDisruptorSymbol(Canvas canvas, float cx, float cy, float size, int color) {
      this.iconRenderer.drawJammerSymbol(canvas, cx, cy, size, color);
   }

   private void drawTinyTreasure(Canvas canvas, float cx, float cy, float size, int color) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(174, 165, 144));
      this.sceneRect.set(cx - size, cy - size * 0.1F, cx + size, cy + size * 0.85F);
      canvas.drawRoundRect(this.sceneRect, 4.0F * this.uiScale, 4.0F * this.uiScale, this.paint);
      this.paint.setColor(Color.argb(120, 160, 218, 232));
      this.sceneRect.set(cx - size * 0.72F, cy - size * 0.95F, cx + size * 0.72F, cy + size * 0.22F);
      canvas.drawRoundRect(this.sceneRect, 5.0F * this.uiScale, 5.0F * this.uiScale, this.paint);
      this.paint.setColor(color);
      canvas.drawCircle(cx, cy - size * 0.32F, size * 0.42F, this.paint);
   }

   private void drawPowerUp(Canvas canvas, PowerUp powerUp) {
      RectF bounds = powerUp.getBounds();
      PowerUp.Type type = powerUp.getType();
      float cx = bounds.centerX();
      float floatOffset = (float)Math.sin((double)(this.levelElapsedSeconds * 3.6F + cx * 0.013F)) * 4.5F;
      float cy = bounds.centerY() + floatOffset;
      float radius = bounds.width() * 0.5F;
      float lifetimeRatio = this.clamp(powerUp.getRemainingSeconds() / 8.0F, 0.0F, 1.0F);
      float pulse = (float)Math.sin((double)(this.levelElapsedSeconds * 6.0F + (float)type.ordinal())) * 0.5F + 0.5F;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(60, 0, 0, 0));
      this.sceneRect.set(cx - radius * 0.74F, bounds.centerY() + radius * 0.5F, cx + radius * 0.74F, bounds.centerY() + radius * 0.88F);
      canvas.drawOval(this.sceneRect, this.paint);
      this.paint.setColor(Color.argb((int)(34.0F + pulse * 28.0F), Color.red(this.powerUpColor(type)), Color.green(this.powerUpColor(type)), Color.blue(this.powerUpColor(type))));
      canvas.drawCircle(cx, cy, radius * (1.42F + pulse * 0.1F), this.paint);
      this.iconRenderer.drawPowerBase(canvas, cx, cy, radius, this.powerUpColor(type), lifetimeRatio);
      this.drawPowerUpSymbol(canvas, type, cx, cy, radius * 0.68F, Color.rgb(14, 18, 22));
      this.paint.setStyle(Style.FILL);
      this.drawWorldCenteredText(canvas, type.getBadge(), cx, cy + radius + 25.0F, radius * 2.1F, 18.0F, Color.rgb(42, 50, 54));
   }

   private void drawCoin(Canvas canvas, Coin coin) {
      if (!coin.isCollected()) {
         RectF bounds = coin.getBounds();
         float pulse = (float)Math.sin((double)(this.levelElapsedSeconds * 5.0F + (float)coin.getValue())) * 0.5F + 0.5F;
         boolean premium = coin.getValue() >= 16;
         float radius = bounds.width() * (premium ? 0.48F : 0.42F);
         float cy = bounds.centerY() - pulse * 3.2F;
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(46, 226, 194, 75));
         canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() * (0.68F + pulse * 0.16F), this.paint);
         this.iconRenderer.drawCoin(canvas, bounds.centerX(), cy, radius, this.levelElapsedSeconds * 5.0F + (float)coin.getValue(), premium, true);
      }
   }

   private void drawDisruptors(Canvas canvas) {
      for(Disruptor disruptor : this.disruptors) {
         float ratio = this.clamp(disruptor.getRemainingSeconds() / 8.0F, 0.0F, 1.0F);
         float pulse = (float)Math.sin((double)(this.levelElapsedSeconds * 8.0F + disruptor.getX() * 0.01F)) * 0.5F + 0.5F;
         float cx = disruptor.getX();
         float cy = disruptor.getY();
         float range = disruptor.getRadius();
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb((int)(32.0F * ratio), 104, 196, 202));
         canvas.drawCircle(cx, cy, range, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeCap(Cap.ROUND);
         this.paint.setStrokeWidth(3.0F);
         this.paint.setColor(Color.argb((int)(112.0F * ratio), 104, 196, 202));
         canvas.drawCircle(cx, cy, range * (0.68F + pulse * 0.12F), this.paint);
         this.paint.setStrokeWidth(1.8F);
         this.paint.setColor(Color.argb((int)(82.0F * ratio), 190, 241, 244));
         canvas.drawCircle(cx, cy, range * (0.4F + pulse * 0.09F), this.paint);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(70, 0, 0, 0));
         this.sceneRect.set(cx - 25.0F, cy + 16.0F, cx + 25.0F, cy + 29.0F);
         canvas.drawOval(this.sceneRect, this.paint);
         this.paint.setColor(Color.rgb(31, 45, 51));
         canvas.drawCircle(cx, cy, 26.0F, this.paint);
         this.paint.setColor(Color.rgb(67, 104, 110));
         canvas.drawCircle(cx, cy, 21.0F, this.paint);
         this.drawDisruptorSymbol(canvas, cx, cy, 21.0F, Color.rgb(104, 196, 202));
         this.paint.setColor(Color.argb((int)(170.0F + pulse * 70.0F), 220, 252, 255));
         canvas.drawCircle(cx - 6.0F, cy - 7.0F, 2.4F + pulse, this.paint);
         this.paint.setStrokeCap(Cap.BUTT);
      }

   }

   private void drawDecoy(Canvas canvas) {
      if (!(this.decoySeconds <= 0.0F)) {
         float ratio = this.clamp(this.decoySeconds / 5.5F, 0.0F, 1.0F);
         float pulse = (float)Math.sin((double)(this.levelElapsedSeconds * 7.0F)) * 0.5F + 0.5F;
         float cx = this.decoyPoint.x;
         float cy = this.decoyPoint.y;
         float radius = 20.0F + pulse * 4.0F;
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb((int)(45.0F * ratio), 226, 194, 75));
         canvas.drawCircle(cx, cy, 51.0F + pulse * 13.0F, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(2.0F);
         this.paint.setColor(Color.argb((int)(115.0F * ratio), 255, 232, 142));
         canvas.drawCircle(cx, cy, 40.0F + pulse * 12.0F, this.paint);
         this.paint.setColor(Color.argb((int)(82.0F * ratio), 255, 241, 180));
         canvas.drawCircle(cx, cy, 27.0F + pulse * 9.0F, this.paint);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(76, 0, 0, 0));
         this.sceneRect.set(cx - 23.0F, cy + 16.0F, cx + 23.0F, cy + 29.0F);
         canvas.drawOval(this.sceneRect, this.paint);
         this.paint.setColor(Color.rgb(70, 57, 34));
         canvas.drawCircle(cx, cy, radius + 4.0F, this.paint);
         this.paint.setColor(Color.rgb(226, 194, 75));
         canvas.drawCircle(cx, cy, radius, this.paint);
         this.paint.setColor(Color.rgb(255, 226, 117));
         this.sceneRect.set(cx - radius * 0.55F, cy - radius * 0.6F, cx + radius * 0.15F, cy + radius * 0.02F);
         canvas.drawOval(this.sceneRect, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(3.0F);
         this.paint.setColor(Color.rgb(83, 61, 20));
         canvas.drawCircle(cx, cy, radius * 0.62F, this.paint);
         canvas.drawLine(cx - 7.0F, cy - 8.0F, cx + 7.0F, cy + 8.0F, this.paint);
         canvas.drawLine(cx + 7.0F, cy - 8.0F, cx - 7.0F, cy + 8.0F, this.paint);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.rgb(255, 244, 190));
         canvas.drawCircle(cx - 6.0F, cy - 7.0F, 2.3F + pulse, this.paint);
         this.drawWorldCenteredText(canvas, "诱饵", cx, cy + radius + 25.0F, 76.0F, 17.0F, Color.rgb(88, 65, 22));
      }
   }

   private void drawDecoyCoinOverlay(Canvas canvas) {
      if (!(this.decoySeconds <= 0.0F)) {
         float pulse = (float)Math.sin((double)(this.levelElapsedSeconds * 7.0F)) * 0.5F + 0.5F;
         float radius = 22.0F + pulse * 7.0F;
         this.iconRenderer.drawCoin(canvas, this.decoyPoint.x, this.decoyPoint.y, radius, this.levelElapsedSeconds * 8.0F, true, true);
      }
   }

   private void drawTreasure(Canvas canvas) {
      List<RectF> treasures = this.getLevel().getTreasures();

      for(int i = 0; i < treasures.size(); ++i) {
         this.drawTreasureCase(canvas, (RectF)treasures.get(i), i, this.state.isTreasureCollected(i));
      }

   }

   private void drawTreasureCase(Canvas canvas, RectF treasure, int index, boolean collected) {
      float size = Math.min(treasure.width(), treasure.height());
      float cx = treasure.centerX();
      float cy = treasure.centerY();
      float baseLeft = cx - size * 0.39F;
      float baseTop = cy - size * 0.18F;
      float baseRight = cx + size * 0.39F;
      float baseBottom = cy + size * 0.35F;
      float glassLeft = cx - size * 0.28F;
      float glassTop = cy - size * 0.62F;
      float glassRight = cx + size * 0.28F;
      float glassBottom = cy + size * 0.14F;
      float pulse = 0.5F + 0.5F * (float)Math.sin((double)(this.levelElapsedSeconds * 2.6F + (float)index));
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(collected ? Color.argb(24, 0, 0, 0) : Color.argb(38 + (int)(18.0F * pulse), 213, 168, 79));
      canvas.drawCircle(cx, cy - size * 0.1F, size * (0.56F + pulse * 0.04F), this.paint);
      this.paint.setColor(Color.argb(68, 0, 0, 0));
      this.sceneRect.set(baseLeft + 8.0F, baseTop + 9.0F, baseRight + 8.0F, baseBottom + 9.0F);
      canvas.drawRoundRect(this.sceneRect, 10.0F, 10.0F, this.paint);
      this.paint.setColor(Color.argb(collected ? 22 : 42, 0, 0, 0));
      this.sceneRect.set(cx - size * 0.41F, cy + size * 0.25F, cx + size * 0.41F, cy + size * 0.49F);
      canvas.drawOval(this.sceneRect, this.paint);
      this.paint.setColor(collected ? Color.rgb(103, 114, 121) : Color.rgb(169, 159, 137));
      this.sceneRect.set(baseLeft, baseTop, baseRight, baseBottom);
      canvas.drawRoundRect(this.sceneRect, 10.0F, 10.0F, this.paint);
      this.paint.setColor(collected ? Color.rgb(76, 86, 93) : Color.rgb(112, 96, 78));
      this.sceneRect.set(baseLeft, cy + size * 0.06F, baseRight, baseBottom);
      canvas.drawRoundRect(this.sceneRect, 9.0F, 9.0F, this.paint);
      this.paint.setColor(collected ? Color.rgb(145, 155, 161) : Color.rgb(207, 198, 174));
      this.sceneRect.set(baseLeft + 5.0F, baseTop + 4.0F, baseRight - 5.0F, cy + size * 0.06F);
      canvas.drawRoundRect(this.sceneRect, 7.0F, 7.0F, this.paint);
      this.paint.setColor(Color.argb(56, 0, 0, 0));
      this.sceneRect.set(cx - size * 0.19F, cy - size * 0.1F, cx + size * 0.19F, cy + size * 0.01F);
      canvas.drawOval(this.sceneRect, this.paint);
      this.paint.setColor(collected ? Color.argb(42, 160, 190, 200) : Color.argb(92, 125, 205, 226));
      this.sceneRect.set(glassLeft, glassTop, glassRight, glassBottom);
      canvas.drawRoundRect(this.sceneRect, 12.0F, 12.0F, this.paint);
      if (!collected) {
         this.paint.setColor(this.exhibitColor(index));
         this.drawExhibitArtifact(canvas, cx, cy - size * 0.22F, size * 0.24F, index, false);
      }

      this.paint.setColor(collected ? Color.argb(30, 255, 255, 255) : Color.argb(52, 255, 255, 255));
      this.scenePath.reset();
      this.scenePath.moveTo(glassLeft + 7.0F, glassTop + 6.0F);
      this.scenePath.lineTo(glassRight - 10.0F, glassTop + 6.0F);
      this.scenePath.lineTo(glassRight - 23.0F, glassBottom - 8.0F);
      this.scenePath.lineTo(glassLeft + 12.0F, glassBottom - 16.0F);
      this.scenePath.close();
      canvas.drawPath(this.scenePath, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(4.0F);
      this.paint.setColor(collected ? Color.rgb(82, 90, 96) : Color.rgb(99, 74, 32));
      this.sceneRect.set(baseLeft, baseTop, baseRight, baseBottom);
      canvas.drawRoundRect(this.sceneRect, 10.0F, 10.0F, this.paint);
      this.paint.setColor(Color.argb(collected ? 72 : 155, 220, 244, 248));
      this.sceneRect.set(glassLeft, glassTop, glassRight, glassBottom);
      canvas.drawRoundRect(this.sceneRect, 12.0F, 12.0F, this.paint);
      this.paint.setStrokeWidth(2.0F);
      this.paint.setColor(Color.argb(collected ? 86 : 180, 255, 255, 255));
      canvas.drawLine(glassLeft + 7.0F, glassTop + 11.0F, glassRight - 9.0F, glassTop + 11.0F, this.paint);
      canvas.drawLine(glassLeft + 10.0F, glassTop + 16.0F, glassRight - 19.0F, glassBottom - 9.0F, this.paint);
      this.paint.setStyle(Style.FILL);
      if (!collected && this.treasureClaimIndex == index) {
         float progress = this.clamp(this.treasureClaimSeconds / this.upgradedTreasureClaimSeconds(), 0.0F, 1.0F);
         this.drawTreasureProgress(canvas, cx, treasure.bottom + 12.0F, treasure.width() * 0.72F, progress);
      } else {
         this.drawWorldCenteredText(canvas, collected ? "已取" : "", cx, treasure.bottom + 24.0F, treasure.width() + 24.0F, 18.0F, collected ? Color.rgb(91, 97, 102) : Color.rgb(82, 66, 28));
      }

   }

   private void drawTreasureProgress(Canvas canvas, float centerX, float centerY, float width, float progress) {
      float height = 9.0F;
      RectF track = new RectF(centerX - width * 0.5F, centerY, centerX + width * 0.5F, centerY + height);
      RectF fill = new RectF(track.left + 2.0F, track.top + 2.0F, track.left + 2.0F + (track.width() - 4.0F) * progress, track.bottom - 2.0F);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(95, 18, 24, 26));
      canvas.drawRoundRect(new RectF(track.left + 2.0F, track.top + 3.0F, track.right + 2.0F, track.bottom + 3.0F), height * 0.5F, height * 0.5F, this.paint);
      this.paint.setColor(Color.argb(175, 242, 244, 238));
      canvas.drawRoundRect(track, height * 0.5F, height * 0.5F, this.paint);
      this.paint.setColor(Color.rgb(58, 153, 105));
      canvas.drawRoundRect(fill, (height - 4.0F) * 0.5F, (height - 4.0F) * 0.5F, this.paint);
      this.paint.setColor(Color.argb(105, 255, 255, 255));
      canvas.drawRoundRect(new RectF(fill.left, fill.top, fill.right, fill.top + 2.0F), 1.2F, 1.2F, this.paint);
      float ringRadius = Math.max(18.0F, width * 0.12F);
      float ringX = centerX + width * 0.5F + ringRadius * 0.9F;
      float ringY = centerY + height * 0.5F;
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeCap(Cap.ROUND);
      this.paint.setStrokeWidth(3.2F);
      this.paint.setColor(Color.argb(90, 18, 24, 26));
      canvas.drawCircle(ringX, ringY, ringRadius, this.paint);
      this.paint.setColor(Color.rgb(104, 196, 202));
      canvas.drawArc(new RectF(ringX - ringRadius, ringY - ringRadius, ringX + ringRadius, ringY + ringRadius), -90.0F, progress * 360.0F, false, this.paint);
      this.paint.setStrokeWidth(1.6F);
      this.paint.setColor(Color.argb(120, 104, 196, 202));
      canvas.drawLine(track.left, track.centerY(), ringX - ringRadius, ringY, this.paint);
      this.paint.setStrokeCap(Cap.BUTT);
   }

   private int exhibitColor(int index) {
      if (index % 3 == 1) {
         return Color.rgb(74, 128, 154);
      } else {
         return index % 3 == 2 ? Color.rgb(154, 74, 104) : Color.rgb(213, 168, 79);
      }
   }

   private void drawExhibitArtifact(Canvas canvas, float centerX, float centerY, float size, int index, boolean collected) {
      int baseColor = this.paint.getColor();
      int lightAlpha = collected ? 68 : 190;
      int shape = Math.floorMod(index, 5);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(baseColor);
      if (shape == 0) {
         canvas.drawCircle(centerX, centerY - size * 0.16F, size * 0.7F, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(Math.max(1.5F, size * 0.13F));
         this.paint.setColor(Color.argb(lightAlpha, 255, 255, 255));
         this.sceneRect.set(centerX - size * 0.62F, centerY - size * 0.7F, centerX + size * 0.62F, centerY + size * 0.42F);
         canvas.drawArc(this.sceneRect, 205.0F, 82.0F, false, this.paint);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.rgb(82, 66, 28));
         this.sceneRect.set(centerX - size * 0.74F, centerY + size * 0.5F, centerX + size * 0.74F, centerY + size * 0.84F);
         canvas.drawRoundRect(this.sceneRect, size * 0.12F, size * 0.12F, this.paint);
      } else if (shape == 1) {
         this.scenePath.reset();
         this.scenePath.moveTo(centerX, centerY - size * 1.02F);
         this.scenePath.lineTo(centerX + size * 0.72F, centerY - size * 0.3F);
         this.scenePath.lineTo(centerX + size * 0.5F, centerY + size * 0.78F);
         this.scenePath.lineTo(centerX, centerY + size * 1.0F);
         this.scenePath.lineTo(centerX - size * 0.5F, centerY + size * 0.78F);
         this.scenePath.lineTo(centerX - size * 0.72F, centerY - size * 0.3F);
         this.scenePath.close();
         canvas.drawPath(this.scenePath, this.paint);
         this.paint.setColor(Color.argb(lightAlpha, 255, 255, 255));
         this.scenePath.reset();
         this.scenePath.moveTo(centerX, centerY - size * 0.88F);
         this.scenePath.lineTo(centerX - size * 0.43F, centerY - size * 0.24F);
         this.scenePath.lineTo(centerX, centerY + size * 0.78F);
         this.scenePath.close();
         canvas.drawPath(this.scenePath, this.paint);
      } else if (shape == 2) {
         this.scenePath.reset();
         this.scenePath.moveTo(centerX, centerY - size * 1.02F);
         this.scenePath.lineTo(centerX + size * 0.88F, centerY + size * 0.42F);
         this.scenePath.lineTo(centerX + size * 0.34F, centerY + size * 0.94F);
         this.scenePath.lineTo(centerX - size * 0.34F, centerY + size * 0.94F);
         this.scenePath.lineTo(centerX - size * 0.88F, centerY + size * 0.42F);
         this.scenePath.close();
         canvas.drawPath(this.scenePath, this.paint);
         this.paint.setColor(Color.argb(lightAlpha, 245, 226, 180));
         canvas.drawCircle(centerX - size * 0.28F, centerY + size * 0.13F, size * 0.13F, this.paint);
         canvas.drawCircle(centerX + size * 0.28F, centerY + size * 0.13F, size * 0.13F, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(Math.max(1.2F, size * 0.1F));
         this.sceneRect.set(centerX - size * 0.38F, centerY + size * 0.16F, centerX + size * 0.38F, centerY + size * 0.62F);
         canvas.drawArc(this.sceneRect, 18.0F, 144.0F, false, this.paint);
         this.paint.setStyle(Style.FILL);
      } else if (shape == 3) {
         this.scenePath.reset();
         this.scenePath.moveTo(centerX - size * 0.3F, centerY - size * 0.92F);
         this.scenePath.lineTo(centerX + size * 0.3F, centerY - size * 0.92F);
         this.scenePath.lineTo(centerX + size * 0.38F, centerY - size * 0.48F);
         this.scenePath.cubicTo(centerX + size * 0.86F, centerY - size * 0.18F, centerX + size * 0.7F, centerY + size * 0.82F, centerX, centerY + size * 0.92F);
         this.scenePath.cubicTo(centerX - size * 0.7F, centerY + size * 0.82F, centerX - size * 0.86F, centerY - size * 0.18F, centerX - size * 0.38F, centerY - size * 0.48F);
         this.scenePath.close();
         canvas.drawPath(this.scenePath, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(Math.max(1.3F, size * 0.11F));
         this.paint.setColor(Color.argb(lightAlpha, 255, 240, 201));
         canvas.drawLine(centerX - size * 0.48F, centerY + size * 0.12F, centerX + size * 0.48F, centerY + size * 0.12F, this.paint);
         this.paint.setStyle(Style.FILL);
      } else {
         this.scenePath.reset();
         this.scenePath.moveTo(centerX - size * 0.9F, centerY + size * 0.62F);
         this.scenePath.lineTo(centerX - size * 0.72F, centerY - size * 0.56F);
         this.scenePath.lineTo(centerX - size * 0.24F, centerY - size * 0.1F);
         this.scenePath.lineTo(centerX, centerY - size * 0.92F);
         this.scenePath.lineTo(centerX + size * 0.24F, centerY - size * 0.1F);
         this.scenePath.lineTo(centerX + size * 0.72F, centerY - size * 0.56F);
         this.scenePath.lineTo(centerX + size * 0.9F, centerY + size * 0.62F);
         this.scenePath.close();
         canvas.drawPath(this.scenePath, this.paint);
         this.paint.setColor(Color.argb(lightAlpha, 255, 243, 187));
         canvas.drawCircle(centerX, centerY + size * 0.26F, size * 0.16F, this.paint);
         canvas.drawCircle(centerX - size * 0.47F, centerY + size * 0.22F, size * 0.12F, this.paint);
         canvas.drawCircle(centerX + size * 0.47F, centerY + size * 0.22F, size * 0.12F, this.paint);
      }
   }

   private void drawExit(Canvas canvas) {
      RectF exit = this.getLevel().getExit();
      boolean active = this.state.hasTreasure();
      boolean countdown = this.exitPromptVisible && !this.exitPromptNeedsTreasure;
      float progress = this.clamp(this.exitStaySeconds / 5.0F, 0.0F, 1.0F);
      float pulse = 0.5F + 0.5F * (float)Math.sin((double)(this.levelElapsedSeconds * 4.0F));
      int green = active ? Color.rgb(58, 153, 105) : Color.rgb(74, 96, 88);
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(active ? 38 + (int)(30.0F * pulse) : 24, 58, 153, 105));
      this.sceneRect.set(exit.left - 24.0F, exit.top - 24.0F, exit.right + 24.0F, exit.bottom + 24.0F);
      canvas.drawRoundRect(this.sceneRect, 16.0F, 16.0F, this.paint);
      this.paint.setColor(Color.argb(active ? 62 : 34, 0, 0, 0));
      this.sceneRect.set(exit.left - 5.0F, exit.bottom - 3.0F, exit.right + 12.0F, exit.bottom + 18.0F);
      canvas.drawOval(this.sceneRect, this.paint);
      this.paint.setColor(Color.rgb(27, 39, 36));
      this.sceneRect.set(exit.left - 5.0F, exit.top - 5.0F, exit.right + 5.0F, exit.bottom + 5.0F);
      canvas.drawRoundRect(this.sceneRect, 12.0F, 12.0F, this.paint);
      this.paint.setColor(green);
      canvas.drawRoundRect(exit, 10.0F, 10.0F, this.paint);
      this.paint.setColor(active ? Color.argb(54, 230, 255, 238) : Color.argb(26, 220, 240, 228));

      for(int i = 0; i < 3; ++i) {
         float inset = 11.0F + (float)i * 18.0F + (active ? pulse * 2.0F : 0.0F);
         this.sceneRect.set(exit.left + inset, exit.top + inset, exit.right - inset, exit.bottom - inset);
         canvas.drawRoundRect(this.sceneRect, 7.0F, 7.0F, this.paint);
      }

      this.paint.setColor(active ? Color.argb(48, 220, 255, 232) : Color.argb(22, 220, 240, 228));
      this.scenePath.reset();
      this.scenePath.moveTo(exit.left + 12.0F, exit.bottom - 13.0F);
      this.scenePath.lineTo(exit.right - 12.0F, exit.bottom - 13.0F);
      this.scenePath.lineTo(exit.right - 26.0F, exit.bottom + 10.0F);
      this.scenePath.lineTo(exit.left + 26.0F, exit.bottom + 10.0F);
      this.scenePath.close();
      canvas.drawPath(this.scenePath, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(countdown ? 7.0F : 5.0F);
      this.paint.setColor(active ? Color.rgb(220, 240, 228) : Color.argb(145, 220, 240, 228));
      canvas.drawRoundRect(exit, 10.0F, 10.0F, this.paint);
      if (countdown) {
         this.paint.setStrokeCap(Cap.ROUND);
         this.paint.setColor(Color.rgb(226, 194, 75));
         this.sceneRect.set(exit.left - 13.0F, exit.top - 13.0F, exit.right + 13.0F, exit.bottom + 13.0F);
         canvas.drawArc(this.sceneRect, -90.0F, 360.0F * progress, false, this.paint);
      }

      this.paint.setStrokeWidth(4.0F);
      this.paint.setStrokeCap(Cap.ROUND);
      this.paint.setColor(-1);
      float arrowX = exit.centerX() + (active ? pulse * 3.0F : 0.0F);
      float arrowY = exit.centerY() - 12.0F;
      canvas.drawLine(arrowX - 25.0F, arrowY, arrowX + 20.0F, arrowY, this.paint);
      canvas.drawLine(arrowX + 20.0F, arrowY, arrowX + 4.0F, arrowY - 16.0F, this.paint);
      canvas.drawLine(arrowX + 20.0F, arrowY, arrowX + 4.0F, arrowY + 16.0F, this.paint);
      this.paint.setStrokeCap(Cap.BUTT);
      this.drawWorldCenteredText(canvas, "撤离", exit.centerX(), exit.centerY() + 33.0F, exit.width() - 10.0F, 23.0F, -1);
   }

   private void drawExitPrompt(Canvas canvas) {
      if (this.appScreen == GameView.AppScreen.PLAYING && this.state.isPlaying() && this.exitPromptVisible) {
         RectF panel = this.exitPromptPanel;
         float progress = this.clamp(this.exitStaySeconds / 5.0F, 0.0F, 1.0F);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(78, 0, 0, 0));
         canvas.drawRoundRect(new RectF(panel.left + 7.0F * this.uiScale, panel.top + 9.0F * this.uiScale, panel.right + 7.0F * this.uiScale, panel.bottom + 9.0F * this.uiScale), 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         this.paint.setColor(Color.argb(232, 9, 14, 18));
         canvas.drawRoundRect(panel, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(2.0F * this.uiScale);
         this.paint.setColor(this.exitPromptNeedsTreasure ? Color.argb(150, 145, 164, 154) : Color.rgb(58, 153, 105));
         canvas.drawRoundRect(panel, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         float ringX = panel.left + 58.0F * this.uiScale;
         float ringY = panel.top + 58.0F * this.uiScale;
         this.drawExitCountdownRing(canvas, ringX, ringY, 28.0F * this.uiScale, progress, this.exitPromptNeedsTreasure);
         String title = this.exitPromptNeedsTreasure ? "撤离点未激活" : (this.state.hasAllTreasures() ? "三星撤离检查" : "确认撤离");
         String subtitle;
         if (this.exitPromptNeedsTreasure) {
            subtitle = "至少取得一件展品后可撤离";
         } else if (this.state.hasAllTreasures()) {
            subtitle = "已取得 " + this.state.getCollectedTreasureCount() + "/" + this.state.getTreasureCount() + "，停留满 5 秒自动撤离";
         } else {
            subtitle = "已取得 " + this.state.getCollectedTreasureCount() + "/" + this.state.getTreasureCount() + "，现在可通关但不是满收集";
         }

         this.drawLeftFittedText(canvas, title, panel.left + 104.0F * this.uiScale, panel.top + 44.0F * this.uiScale, panel.width() - 124.0F * this.uiScale, 25.0F * this.uiScale, -1);
         this.drawLeftFittedText(canvas, subtitle, panel.left + 104.0F * this.uiScale, panel.top + 75.0F * this.uiScale, panel.width() - 124.0F * this.uiScale, 17.0F * this.uiScale, Color.rgb(220, 228, 226));
         if (this.exitPromptNeedsTreasure) {
            this.drawCenteredFittedText(canvas, "继续寻找展柜", panel.centerX(), panel.bottom - 31.0F * this.uiScale, panel.width() - 44.0F * this.uiScale, 20.0F * this.uiScale, Color.rgb(145, 164, 154));
         } else {
            this.drawExitPromptButton(canvas, this.exitContinueButton, "继续盗取", Color.rgb(26, 36, 42), Color.rgb(104, 196, 202));
            this.drawExitPromptButton(canvas, this.exitConfirmButton, "立即撤离", Color.rgb(34, 64, 48), Color.rgb(58, 153, 105));
         }
      }
   }

   private void drawExitCountdownRing(Canvas canvas, float cx, float cy, float radius, float progress, boolean disabled) {
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeCap(Cap.ROUND);
      this.paint.setStrokeWidth(6.0F * this.uiScale);
      this.paint.setColor(disabled ? Color.argb(90, 145, 164, 154) : Color.argb(90, 220, 240, 228));
      canvas.drawCircle(cx, cy, radius, this.paint);
      this.paint.setColor(disabled ? Color.rgb(145, 164, 154) : Color.rgb(226, 194, 75));
      canvas.drawArc(new RectF(cx - radius, cy - radius, cx + radius, cy + radius), -90.0F, 360.0F * progress, false, this.paint);
      this.paint.setStrokeCap(Cap.BUTT);
      String label = disabled ? "!" : String.format(Locale.US, "%.1f", Math.max(0.0F, 5.0F - this.exitStaySeconds));
      this.drawCenteredFittedText(canvas, label, cx, cy + 7.0F * this.uiScale, radius * 1.45F, 17.0F * this.uiScale, -1);
   }

   private void drawExitPromptButton(Canvas canvas, RectF bounds, String label, int fillColor, int accentColor) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(fillColor);
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.6F * this.uiScale);
      this.paint.setColor(accentColor);
      canvas.drawRoundRect(bounds, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.drawCenteredFittedText(canvas, label, bounds.centerX(), bounds.centerY() + 7.0F * this.uiScale, bounds.width() - 16.0F * this.uiScale, 18.0F * this.uiScale, -1);
   }

   private void drawPlayer(Canvas canvas) {
      this.actorRenderer.drawPlayer(canvas, this.player, this.selectedCharacter, this.state.isInvisible(), this.state.canPhaseThroughWalls(), this.state.isBoosting(), this.state.isSpeedPotionActive(), this.state.getCollectedTreasureCount(), this.levelElapsedSeconds);
   }

   private void drawGuard(Canvas canvas, Guard guard) {
      this.actorRenderer.drawGuard(canvas, guard, this.levelElapsedSeconds);
   }

   private void drawStaff(Canvas canvas, Guard guard) {
      float x = guard.getX();
      float y = guard.getY();
      float facingX = guard.getFacingX();
      float facingY = guard.getFacingY();
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(72, 0, 0, 0));
      canvas.drawOval(new RectF(x - 29.0F, y + 18.0F, x + 29.0F, y + 36.0F), this.paint);
      this.paint.setColor(Color.rgb(44, 78, 91));
      canvas.drawRoundRect(new RectF(x - 17.0F, y - 2.0F, x + 17.0F, y + 31.0F), 9.0F, 9.0F, this.paint);
      this.paint.setColor(Color.rgb(230, 236, 230));
      canvas.drawRoundRect(new RectF(x - 12.0F, y + 2.0F, x + 12.0F, y + 16.0F), 5.0F, 5.0F, this.paint);
      this.paint.setColor(Color.rgb(226, 194, 75));
      canvas.drawRoundRect(new RectF(x + 5.0F, y + 9.0F, x + 15.0F, y + 17.0F), 2.0F, 2.0F, this.paint);
      this.paint.setColor(Color.rgb(220, 166, 122));
      canvas.drawCircle(x, y - 15.0F, 13.0F, this.paint);
      this.paint.setColor(Color.rgb(44, 36, 30));
      canvas.drawOval(new RectF(x - 12.0F, y - 29.0F, x + 12.0F, y - 16.0F), this.paint);
      this.paint.setColor(Color.rgb(26, 32, 38));
      canvas.drawRoundRect(new RectF(x - 14.0F, y - 32.0F, x + 14.0F, y - 25.0F), 3.0F, 3.0F, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeCap(Cap.ROUND);
      this.paint.setStrokeWidth(5.0F);
      this.paint.setColor(Color.rgb(34, 47, 54));
      canvas.drawLine(x - 10.0F, y + 27.0F, x - 16.0F, y + 38.0F, this.paint);
      canvas.drawLine(x + 10.0F, y + 27.0F, x + 16.0F, y + 38.0F, this.paint);
      this.paint.setStrokeWidth(6.0F);
      canvas.drawLine(x, y + 4.0F, x + facingX * 30.0F, y + 4.0F + facingY * 30.0F, this.paint);
      this.paint.setStrokeWidth(3.0F);
      this.paint.setColor(Color.rgb(244, 236, 188));
      canvas.drawLine(x + facingX * 22.0F, y + facingY * 22.0F, x + facingX * 40.0F, y + facingY * 40.0F, this.paint);
      this.paint.setStrokeCap(Cap.BUTT);
      this.drawGuardAlertBadge(canvas, guard, x, y - 48.0F);
   }

   private void drawGuardAlertBadge(Canvas canvas, Guard guard, float x, float y) {
      float progress = guard.getSuspicionProgress();
      if (!(progress <= 0.01F) || guard.getAlertState() != AlertState.PATROL) {
         float width = 52.0F;
         float height = 8.0F;
         RectF bar = new RectF(x - width * 0.5F, y, x + width * 0.5F, y + height);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(175, 8, 10, 12));
         canvas.drawRoundRect(bar, 4.0F, 4.0F, this.paint);
         int color = guard.getAlertState() == AlertState.ALERT ? Color.rgb(235, 88, 80) : Color.rgb(226, 194, 75);
         this.paint.setColor(color);
         canvas.drawRoundRect(new RectF(bar.left, bar.top, bar.left + bar.width() * progress, bar.bottom), 4.0F, 4.0F, this.paint);
         this.drawWorldCenteredText(canvas, guard.getAlertState() == AlertState.ALERT ? "!" : "?", x, y - 4.0F, 28.0F, 16.0F, color);
      }
   }

   private void drawGuardVision(Canvas canvas, Guard guard) {
      if (!guard.isDisabled()) {
         float facingAngle = (float)Math.atan2((double)guard.getFacingY(), (double)guard.getFacingX());
         float viewAngle = guard.getViewAngleRadians() + (guard.getKind() == Kind.ROBOT ? 0.18F : 0.07F);
         float viewDistance = guard.getViewDistance() + (guard.getKind() == Kind.ROBOT ? this.player.getRadius() * 0.85F : this.player.getRadius() * 0.28F);
         this.buildVisionPath(guard.getX(), guard.getY(), facingAngle, viewAngle, viewDistance, this.getLevel());
         boolean distracted = this.isDetectorDistracted(guard.getX(), guard.getY(), guard.getFacingX(), guard.getFacingY(), viewDistance, viewAngle, this.getLevel());
         this.paint.setStyle(Style.FILL);
         int fillAlpha = distracted ? 42 : (this.state.isInvisible() ? 36 : (this.highContrastVision ? 118 : 70));
         if (distracted) {
            this.paint.setColor(Color.argb(fillAlpha, 226, 194, 75));
         } else if (guard.getKind() == Kind.STAFF) {
            this.paint.setColor(Color.argb(fillAlpha, 44, 126, 150));
         } else {
            this.paint.setColor(Color.argb(fillAlpha, 224, 70, 58));
         }

         canvas.drawPath(this.visionPath, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(3.0F);
         int strokeAlpha = this.state.isInvisible() ? 92 : (this.highContrastVision ? 210 : 145);
         if (distracted) {
            this.paint.setColor(Color.argb(160, 226, 194, 75));
         } else if (guard.getKind() == Kind.STAFF) {
            this.paint.setColor(Color.argb(strokeAlpha, 44, 126, 150));
         } else {
            this.paint.setColor(Color.argb(strokeAlpha, 224, 70, 58));
         }

         canvas.drawPath(this.visionPath, this.paint);
      }
   }

   private void drawCameraVision(Canvas canvas, SecurityCamera camera) {
      if (!this.isInsideDisruptor(camera.getX(), camera.getY())) {
         this.buildVisionPath(camera.getX(), camera.getY(), camera.getCurrentAngleRadians(), camera.getViewAngleRadians(), camera.getViewDistance(), this.getLevel());
         boolean distracted = this.isDetectorDistracted(camera.getX(), camera.getY(), camera.getFacingX(), camera.getFacingY(), camera.getViewDistance(), camera.getViewAngleRadians(), this.getLevel());
         this.paint.setStyle(Style.FILL);
         int fillAlpha = distracted ? 38 : (this.state.isInvisible() ? 34 : (this.highContrastVision ? 108 : 68));
         this.paint.setColor(distracted ? Color.argb(fillAlpha, 226, 194, 75) : Color.argb(fillAlpha, 77, 135, 220));
         canvas.drawPath(this.visionPath, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(3.0F);
         int strokeAlpha = distracted ? 148 : (this.state.isInvisible() ? 92 : (this.highContrastVision ? 220 : 165));
         this.paint.setColor(distracted ? Color.argb(strokeAlpha, 226, 194, 75) : Color.argb(strokeAlpha, 77, 135, 220));
         canvas.drawPath(this.visionPath, this.paint);
      }
   }

   private void drawCamera(Canvas canvas, SecurityCamera camera) {
      boolean disrupted = this.isInsideDisruptor(camera.getX(), camera.getY());
      this.actorRenderer.drawCamera(canvas, camera, disrupted, this.levelElapsedSeconds);
   }

   private void drawCameraAlertBadge(Canvas canvas, SecurityCamera camera) {
      float progress = camera.getSuspicionProgress();
      if (!(progress <= 0.01F) || camera.getAlertState() != com.museumheist.game.entity.SecurityCamera.AlertState.PATROL) {
         float width = 46.0F;
         RectF bar = new RectF(camera.getX() - width * 0.5F, camera.getY() - 38.0F, camera.getX() + width * 0.5F, camera.getY() - 30.0F);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(175, 8, 10, 12));
         canvas.drawRoundRect(bar, 4.0F, 4.0F, this.paint);
         this.paint.setColor(camera.getAlertState() == com.museumheist.game.entity.SecurityCamera.AlertState.DISABLED ? Color.rgb(104, 196, 202) : Color.rgb(77, 135, 220));
         canvas.drawRoundRect(new RectF(bar.left, bar.top, bar.left + bar.width() * progress, bar.bottom), 4.0F, 4.0F, this.paint);
      }
   }

   private void buildVisionPath(float originX, float originY, float facingAngle, float viewAngleRadians, float viewDistance, Level level) {
      float halfAngle = viewAngleRadians * 0.5F;
      int segments = 18;
      this.visionPath.reset();
      this.visionPath.moveTo(originX, originY);

      for(int i = 0; i <= segments; ++i) {
         float angle = facingAngle - halfAngle + viewAngleRadians * (float)i / (float)segments;
         float distance = this.castVisionDistance(originX, originY, angle, viewDistance, level);
         this.visionPath.lineTo(originX + (float)Math.cos((double)angle) * distance, originY + (float)Math.sin((double)angle) * distance);
      }

      this.visionPath.close();
   }

   private float castVisionDistance(float originX, float originY, float angle, float maxDistance, Level level) {
      float step = 18.0F;

      for(float distance = step; distance <= maxDistance; distance += step) {
         float x = originX + (float)Math.cos((double)angle) * distance;
         float y = originY + (float)Math.sin((double)angle) * distance;
         if (this.isBlockingPoint(x, y, level)) {
            return Math.max(0.0F, distance - step * 0.55F);
         }
      }

      return maxDistance;
   }

   private boolean isBlockingPoint(float x, float y, Level level) {
      for(Wall wall : level.getWalls()) {
         if (wall.isBlocking() && wall.getBounds().contains(x, y)) {
            return true;
         }
      }

      for(Door door : level.getDoors()) {
         if (door.isBlocking() && door.getBounds().contains(x, y)) {
            return true;
         }
      }

      return false;
   }

   private void drawHudPanelV10(Canvas canvas) {
      this.hudRenderer.drawHud(canvas, this.hudPanel, this.uiScale, this.getLevel(), this.levelManager.getCurrentLevelNumber(), this.levelManager.getLevelCount(), this.currentObjective(), this.coinsBalance, this.stealthTracker.getDisplayedThreat(), this.stealthTracker.getThreatBand(), this.stealthTracker.getChain(), this.stealthTracker.getChainProgress(), this);
   }

   private void drawTimerPanelV10(Canvas canvas) {
      if (this.appScreen == GameView.AppScreen.PLAYING) {
         this.hudRenderer.drawTimerPanel(canvas, this.timerPanel, this.uiScale, this.levelElapsedSeconds, this);
      }
   }

   private String currentObjective() {
      if (this.state.isPaused()) {
         return "已暂停";
      } else if (this.state.isCleared()) {
         return this.levelManager.hasNextLevel() ? "展厅已清空，下一处入口开放。" : "全部展品已带出，可重新开始行动。";
      } else if (this.state.isFailed()) {
         return this.state.getFailureReason();
      } else if (this.state.hasAllTreasures()) {
         return "三件展品已到手，前往绿色撤离区。";
      } else if (this.state.hasTreasure()) {
         return "已取得 " + this.state.getCollectedTreasureCount() + "/" + this.state.getTreasureCount() + " 件展品，可撤离或继续冲三星。";
      } else {
         return this.state.hasKey() ? "钥匙已入物品栏，靠近同色门并点击门可开关。" : this.getLevel().getObjective() + " 展柜正在接入时保持位置即可。";
      }
   }

   private void drawTopButtons(Canvas canvas) {
      if (this.appScreen == GameView.AppScreen.PLAYING) {
         this.drawIconButton(canvas, this.homeButton, "home");
         this.drawIconButton(canvas, this.pauseButton, this.state.isPaused() ? "play" : "pause");
         this.drawIconButton(canvas, this.restartButton, "restart");
      }
   }

   private void drawInventoryBar(Canvas canvas) {
      if (this.appScreen == GameView.AppScreen.PLAYING && !this.hotbarSlots.isEmpty()) {
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(142, 8, 11, 16));
         canvas.drawRoundRect(this.inventoryPanel, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(1.1F * this.uiScale);
         this.paint.setColor(Color.argb(105, 238, 240, 232));
         canvas.drawRoundRect(this.inventoryPanel, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         GameState.HotbarSlot[] slots = this.state.getHotbar();

         for(int i = 0; i < this.hotbarSlots.size(); ++i) {
            RectF slotBounds = (RectF)this.hotbarSlots.get(i);
            GameState.HotbarSlot slot = i < slots.length ? slots[i] : null;
            boolean empty = slot == null || slot.getType() == HotbarType.EMPTY;
            this.paint.setStyle(Style.FILL);
            this.paint.setColor(empty ? Color.argb(104, 22, 28, 34) : Color.argb(212, 238, 240, 232));
            canvas.drawRoundRect(slotBounds, 7.0F * this.uiScale, 7.0F * this.uiScale, this.paint);
            this.paint.setStyle(Style.STROKE);
            this.paint.setStrokeWidth(1.4F * this.uiScale);
            this.paint.setColor(empty ? Color.argb(90, 238, 240, 232) : Color.rgb(213, 168, 79));
            canvas.drawRoundRect(slotBounds, 7.0F * this.uiScale, 7.0F * this.uiScale, this.paint);
            if (empty) {
               this.drawCenteredFittedText(canvas, String.valueOf(i + 1), slotBounds.centerX(), slotBounds.centerY() + 6.0F * this.uiScale, slotBounds.width() - 10.0F * this.uiScale, 15.0F * this.uiScale, Color.argb(135, 220, 226, 228));
            } else if (slot.getType() == HotbarType.KEY) {
               this.drawKeySymbol(canvas, slotBounds.centerX(), slotBounds.centerY() - 1.0F * this.uiScale, slotBounds.width() * 0.28F, slot.getColor());
               this.drawCenteredFittedText(canvas, "钥", slotBounds.centerX(), slotBounds.bottom - 6.0F * this.uiScale, slotBounds.width() - 8.0F * this.uiScale, 12.0F * this.uiScale, Color.rgb(36, 42, 48));
            } else {
               this.drawTinyTreasure(canvas, slotBounds.centerX(), slotBounds.centerY() - 2.0F * this.uiScale, slotBounds.width() * 0.25F, this.exhibitColor(slot.getTreasureIndex()));
               this.drawCenteredFittedText(canvas, String.valueOf(slot.getTreasureIndex() + 1), slotBounds.centerX(), slotBounds.bottom - 6.0F * this.uiScale, slotBounds.width() - 8.0F * this.uiScale, 12.0F * this.uiScale, Color.rgb(36, 42, 48));
            }
         }

         this.drawCenteredFittedText(canvas, "物品栏 5", this.inventoryPanel.centerX(), this.inventoryPanel.top - 7.0F * this.uiScale, this.inventoryPanel.width(), 12.0F * this.uiScale, Color.argb(170, 225, 230, 232));
      }
   }

   private void drawMiniMap(Canvas canvas) {
      Level level = this.getLevel();
      RectF bounds = level.getBounds();
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(170, 9, 12, 17));
      canvas.drawRoundRect(this.miniMapPanel, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.3F * this.uiScale);
      this.paint.setColor(Color.argb(130, 238, 240, 232));
      canvas.drawRoundRect(this.miniMapPanel, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      float scaleX = (this.miniMapPanel.width() - 14.0F * this.uiScale) / level.getWorldWidth();
      float scaleY = (this.miniMapPanel.height() - 14.0F * this.uiScale) / level.getWorldHeight();
      float scale = Math.min(scaleX, scaleY);
      float left = this.miniMapPanel.left + (this.miniMapPanel.width() - level.getWorldWidth() * scale) * 0.5F;
      float top = this.miniMapPanel.top + (this.miniMapPanel.height() - level.getWorldHeight() * scale) * 0.5F;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.rgb(201, 194, 176));
      canvas.drawRect(left + bounds.left * scale, top + bounds.top * scale, left + bounds.right * scale, top + bounds.bottom * scale, this.paint);
      this.paint.setColor(Color.rgb(78, 67, 58));

      for(Wall wall : level.getWalls()) {
         RectF wallBounds = wall.getBounds();
         canvas.drawRect(left + wallBounds.left * scale, top + wallBounds.top * scale, left + wallBounds.right * scale, top + wallBounds.bottom * scale, this.paint);
      }

      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.2F * this.uiScale);
      this.paint.setColor(Color.argb(185, 255, 255, 255));
      canvas.drawRect(left + this.cameraX * scale, top + this.cameraY * scale, left + (this.cameraX + (float)this.screenWidth / this.worldScale) * scale, top + (this.cameraY + (float)this.screenHeight / this.worldScale) * scale, this.paint);
      this.paint.setStyle(Style.FILL);

      for(int i = 0; i < level.getTreasures().size(); ++i) {
         RectF treasure = (RectF)level.getTreasures().get(i);
         this.paint.setColor(this.state.isTreasureCollected(i) ? Color.rgb(112, 123, 130) : this.exhibitColor(i));
         canvas.drawCircle(left + treasure.centerX() * scale, top + treasure.centerY() * scale, 3.8F * this.uiScale, this.paint);
      }

      if (this.decoySeconds > 0.0F) {
         this.paint.setColor(Color.rgb(226, 194, 75));
         canvas.drawCircle(left + this.decoyPoint.x * scale, top + this.decoyPoint.y * scale, 3.5F * this.uiScale, this.paint);
      }

      this.paint.setColor(Color.rgb(226, 194, 75));

      for(Coin coin : this.coins) {
         if (!coin.isCollected()) {
            RectF coinBounds = coin.getBounds();
            canvas.drawCircle(left + coinBounds.centerX() * scale, top + coinBounds.centerY() * scale, 2.2F * this.uiScale, this.paint);
         }
      }

      this.paint.setColor(Color.rgb(104, 196, 202));

      for(Disruptor disruptor : this.disruptors) {
         canvas.drawCircle(left + disruptor.getX() * scale, top + disruptor.getY() * scale, 3.0F * this.uiScale, this.paint);
      }

      this.paint.setColor(Color.rgb(58, 153, 105));
      RectF exit = level.getExit();
      canvas.drawCircle(left + exit.centerX() * scale, top + exit.centerY() * scale, 4.0F * this.uiScale, this.paint);

      for(Guard guard : level.getGuards()) {
         this.paint.setColor(guard.getKind() == Kind.STAFF ? Color.rgb(44, 126, 150) : Color.rgb(151, 54, 54));
         canvas.drawCircle(left + guard.getX() * scale, top + guard.getY() * scale, 2.8F * this.uiScale, this.paint);
      }

      this.paint.setColor(Color.rgb(38, 111, 190));
      canvas.drawCircle(left + this.player.getX() * scale, top + this.player.getY() * scale, 4.2F * this.uiScale, this.paint);
   }

   private void drawIconButton(Canvas canvas, RectF bounds, String label) {
      float radius = bounds.width() * 0.5F;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(150, 9, 12, 17));
      canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.5F * this.uiScale);
      this.paint.setColor(Color.argb(135, 238, 240, 232));
      canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius - 1.5F * this.uiScale, this.paint);
      this.paint.setColor(Color.rgb(242, 244, 238));
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(3.4F * this.uiScale);
      this.paint.setStrokeCap(Cap.ROUND);
      this.paint.setStrokeJoin(Join.ROUND);
      float cx = bounds.centerX();
      float cy = bounds.centerY();
      float s = bounds.width() * 0.28F;
      if ("home".equals(label)) {
         Path roof = new Path();
         roof.moveTo(cx - s, cy - s * 0.05F);
         roof.lineTo(cx, cy - s * 0.95F);
         roof.lineTo(cx + s, cy - s * 0.05F);
         canvas.drawPath(roof, this.paint);
         canvas.drawLine(cx - s * 0.68F, cy - s * 0.02F, cx - s * 0.68F, cy + s * 0.84F, this.paint);
         canvas.drawLine(cx + s * 0.68F, cy - s * 0.02F, cx + s * 0.68F, cy + s * 0.84F, this.paint);
         canvas.drawLine(cx - s * 0.68F, cy + s * 0.84F, cx + s * 0.68F, cy + s * 0.84F, this.paint);
      } else if ("pause".equals(label)) {
         canvas.drawLine(cx - s * 0.42F, cy - s * 0.78F, cx - s * 0.42F, cy + s * 0.78F, this.paint);
         canvas.drawLine(cx + s * 0.42F, cy - s * 0.78F, cx + s * 0.42F, cy + s * 0.78F, this.paint);
      } else if ("play".equals(label)) {
         this.paint.setStyle(Style.FILL);
         Path play = new Path();
         play.moveTo(cx - s * 0.45F, cy - s * 0.82F);
         play.lineTo(cx - s * 0.45F, cy + s * 0.82F);
         play.lineTo(cx + s * 0.85F, cy);
         play.close();
         canvas.drawPath(play, this.paint);
      } else if ("restart".equals(label)) {
         RectF arc = new RectF(cx - s, cy - s, cx + s, cy + s);
         canvas.drawArc(arc, 35.0F, 285.0F, false, this.paint);
         Path arrow = new Path();
         arrow.moveTo(cx + s * 0.82F, cy - s * 0.72F);
         arrow.lineTo(cx + s * 0.82F, cy - s * 0.08F);
         arrow.lineTo(cx + s * 0.22F, cy - s * 0.4F);
         canvas.drawPath(arrow, this.paint);
      }

      this.paint.setStrokeCap(Cap.BUTT);
      this.paint.setStrokeJoin(Join.MITER);
   }

   private void drawButton(Canvas canvas, RectF bounds, String label) {
      float radius = 9.0F * this.uiScale;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(70, 0, 0, 0));
      this.sceneRect.set(bounds.left + 3.0F * this.uiScale, bounds.top + 4.0F * this.uiScale, bounds.right + 3.0F * this.uiScale, bounds.bottom + 4.0F * this.uiScale);
      canvas.drawRoundRect(this.sceneRect, radius, radius, this.paint);
      this.paint.setColor(Color.rgb(31, 47, 56));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.paint.setColor(Color.argb(26, 255, 255, 255));
      this.sceneRect.set(bounds.left + 3.0F * this.uiScale, bounds.top + 3.0F * this.uiScale, bounds.right - 3.0F * this.uiScale, bounds.centerY());
      canvas.drawRoundRect(this.sceneRect, 7.0F * this.uiScale, 7.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(1.5F * this.uiScale);
      this.paint.setColor(Color.argb(168, 105, 151, 158));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setTextSize(this.fitTextSize(label, bounds.width() - 18.0F * this.uiScale, 21.0F * this.uiScale));
      Paint.FontMetrics metrics = this.paint.getFontMetrics();
      float baseline = bounds.centerY() - (metrics.ascent + metrics.descent) * 0.5F;
      this.paint.setColor(Color.rgb(233, 241, 239));
      this.paint.setTextAlign(Align.CENTER);
      canvas.drawText(label, bounds.centerX(), baseline, this.paint);
      this.paint.setTextAlign(Align.LEFT);
   }

   private void drawPrimaryButton(Canvas canvas, RectF bounds, String label) {
      float radius = 10.0F * this.uiScale;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(92, 0, 0, 0));
      this.sceneRect.set(bounds.left + 4.0F * this.uiScale, bounds.top + 6.0F * this.uiScale, bounds.right + 4.0F * this.uiScale, bounds.bottom + 6.0F * this.uiScale);
      canvas.drawRoundRect(this.sceneRect, radius, radius, this.paint);
      this.paint.setColor(Color.rgb(65, 184, 177));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.paint.setColor(Color.argb(55, 255, 255, 255));
      this.sceneRect.set(bounds.left + 4.0F * this.uiScale, bounds.top + 4.0F * this.uiScale, bounds.right - 4.0F * this.uiScale, bounds.centerY());
      canvas.drawRoundRect(this.sceneRect, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F * this.uiScale);
      this.paint.setColor(Color.rgb(151, 239, 232));
      canvas.drawRoundRect(bounds, radius, radius, this.paint);
      this.paint.setStyle(Style.FILL);
      this.paint.setTextSize(this.fitTextSize(label, bounds.width() - 20.0F * this.uiScale, 22.0F * this.uiScale));
      Paint.FontMetrics metrics = this.paint.getFontMetrics();
      this.paint.setTextAlign(Align.CENTER);
      this.paint.setColor(Color.rgb(8, 31, 33));
      canvas.drawText(label, bounds.centerX(), bounds.centerY() - (metrics.ascent + metrics.descent) * 0.5F, this.paint);
      this.paint.setTextAlign(Align.LEFT);
   }

   private void drawMessageBanner(Canvas canvas) {
      if (!(this.messageSeconds <= 0.0F) && this.messageText.length() != 0) {
         float alpha = this.clamp(this.messageSeconds / 0.35F, 0.0F, 1.0F);
         float bannerHeight = 42.0F * this.uiScale;
         float bannerWidth = Math.min((float)this.getWidth() * 0.48F, 620.0F * this.uiScale);
         float bannerLeft = (float)this.getWidth() * 0.5F - bannerWidth * 0.5F;
         float bannerTop = this.appScreen == GameView.AppScreen.PLAYING ? Math.max(this.hudPanel.bottom, this.miniMapPanel.bottom) + 12.0F * this.uiScale : 18.0F * this.uiScale;
         bannerTop = Math.min(bannerTop, (float)this.getHeight() - bannerHeight - 18.0F * this.uiScale);
         this.sceneRect.set(bannerLeft, bannerTop, bannerLeft + bannerWidth, bannerTop + bannerHeight);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb((int)(198.0F * alpha), 12, 16, 20));
         canvas.drawRoundRect(this.sceneRect, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(1.5F * this.uiScale);
         this.paint.setColor(this.withAlpha(this.messageColor, (int)(150.0F * alpha)));
         canvas.drawRoundRect(this.sceneRect, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
         this.drawCenteredFittedText(canvas, this.messageText, this.sceneRect.centerX(), this.sceneRect.centerY() + 7.0F * this.uiScale, this.sceneRect.width() - 22.0F * this.uiScale, 20.0F * this.uiScale, this.withAlpha(this.messageColor, (int)(255.0F * alpha)));
      }
   }

   private void drawActionButtons(Canvas canvas) {
      if (this.appScreen == GameView.AppScreen.PLAYING && this.state.isPlaying()) {
         for(int i = this.powerUpButtons.size() - 1; i >= 0; --i) {
            this.drawItemButton(canvas, i, (RectF)this.powerUpButtons.get(i));
         }

         this.drawBoostButton(canvas);
         PowerUp.Type activeType = this.state.getActivePowerUp();
         boolean showingSpeed = activeType == null && this.decoySeconds <= 0.0F && this.state.isSpeedPotionActive();
         boolean showingDisruptor = activeType == null && this.decoySeconds <= 0.0F && !showingSpeed && !this.disruptors.isEmpty();
         if (activeType != null || this.decoySeconds > 0.0F || showingSpeed || showingDisruptor) {
            float panelWidth = 214.0F * this.uiScale;
            float panelHeight = 38.0F * this.uiScale;
            RectF activePanel = new RectF(this.boostButton.right - panelWidth, this.boostButton.top - panelHeight - 12.0F * this.uiScale, this.boostButton.right, this.boostButton.top - 12.0F * this.uiScale);
            boolean showingDecoy = activeType == null && this.decoySeconds > 0.0F;
            PowerUp.Type symbolType = showingDecoy ? Type.DECOY : (showingSpeed ? Type.SPEED : (showingDisruptor ? Type.JAMMER : activeType));
            this.paint.setStyle(Style.FILL);
            this.paint.setColor(Color.argb(170, 9, 12, 17));
            canvas.drawRoundRect(activePanel, 8.0F * this.uiScale, 8.0F * this.uiScale, this.paint);
            this.drawPowerUpSymbol(canvas, symbolType, activePanel.left + 22.0F * this.uiScale, activePanel.centerY(), 12.0F * this.uiScale, Color.rgb(238, 244, 242));
            this.drawLeftFittedText(canvas, this.activeStatusText(activeType, showingDecoy, showingSpeed, showingDisruptor), activePanel.left + 42.0F * this.uiScale, activePanel.centerY() + 6.0F * this.uiScale, activePanel.width() - 50.0F * this.uiScale, 17.0F * this.uiScale, Color.rgb(230, 236, 238));
         }

      }
   }

   private void drawBoostButton(Canvas canvas) {
      float cx = this.boostButton.centerX();
      float cy = this.boostButton.centerY();
      float radius = this.boostButton.width() * 0.5F;
      float energy = this.state.getBoostEnergy();
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(76, 0, 0, 0));
      canvas.drawCircle(cx + 6.0F * this.uiScale, cy + 8.0F * this.uiScale, radius, this.paint);
      this.paint.setColor(this.state.isBoosting() ? Color.rgb(118, 92, 32) : Color.argb(176, 10, 14, 20));
      canvas.drawCircle(cx, cy, radius, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(6.0F * this.uiScale);
      this.paint.setColor(Color.argb(96, 238, 240, 232));
      canvas.drawCircle(cx, cy, radius - 5.0F * this.uiScale, this.paint);
      this.paint.setStrokeCap(Cap.ROUND);
      this.paint.setColor(energy >= 1.0F ? Color.rgb(226, 194, 75) : Color.rgb(104, 196, 202));
      canvas.drawArc(new RectF(cx - radius + 6.0F * this.uiScale, cy - radius + 6.0F * this.uiScale, cx + radius - 6.0F * this.uiScale, cy + radius - 6.0F * this.uiScale), -90.0F, 360.0F * energy, false, this.paint);
      this.paint.setStrokeCap(Cap.BUTT);
      this.drawBoostSymbol(canvas, cx, cy, radius * 0.52F, this.state.isBoosting() ? Color.rgb(255, 244, 185) : Color.rgb(238, 244, 242));
      this.drawCenteredFittedText(canvas, this.state.isBoosting() ? "加速" : Math.round(energy * 100.0F) + "%", cx, this.boostButton.top - 8.0F * this.uiScale, this.boostButton.width() + 22.0F * this.uiScale, 15.0F * this.uiScale, Color.rgb(230, 236, 238));
   }

   private String activeStatusText(PowerUp.Type activeType, boolean showingDecoy, boolean showingSpeed, boolean showingDisruptor) {
      if (showingDecoy) {
         return "诱饵吸引 " + Math.max(1, (int)Math.ceil((double)this.decoySeconds)) + "s";
      } else if (showingSpeed) {
         return "加速药剂 " + Math.max(1, (int)Math.ceil((double)this.state.getSpeedPotionSeconds())) + "s";
      } else if (!showingDisruptor) {
         return activeType.getLabel() + " " + Math.max(1, (int)Math.ceil((double)this.state.getPowerUpSeconds())) + "s";
      } else {
         float remaining = 0.0F;

         for(Disruptor disruptor : this.disruptors) {
            remaining = Math.max(remaining, disruptor.getRemainingSeconds());
         }

         return "干扰范围 " + Math.max(1, (int)Math.ceil((double)remaining)) + "s";
      }
   }

   private void drawItemButton(Canvas canvas, int slotIndex, RectF bounds) {
      PowerUp.Type stored = this.state.getStoredPowerUp(slotIndex);
      float cx = bounds.centerX();
      float cy = bounds.centerY();
      float radius = bounds.width() * 0.5F;
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(70, 0, 0, 0));
      canvas.drawCircle(cx + 5.0F * this.uiScale, cy + 7.0F * this.uiScale, radius, this.paint);
      this.paint.setColor(stored == null ? Color.argb(154, 10, 14, 20) : this.powerUpColor(stored));
      canvas.drawCircle(cx, cy, radius, this.paint);
      this.paint.setStyle(Style.STROKE);
      this.paint.setStrokeWidth(2.0F * this.uiScale);
      this.paint.setColor(stored == null ? Color.argb(118, 238, 240, 232) : Color.argb(210, 255, 255, 255));
      canvas.drawCircle(cx, cy, radius - 2.0F * this.uiScale, this.paint);
      if (stored == null) {
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeCap(Cap.ROUND);
         this.paint.setStrokeWidth(4.0F * this.uiScale);
         this.paint.setColor(Color.rgb(180, 188, 192));
         this.sceneRect.set(cx - radius * 0.42F, cy - radius * 0.35F, cx + radius * 0.42F, cy + radius * 0.35F);
         canvas.drawRoundRect(this.sceneRect, 6.0F * this.uiScale, 6.0F * this.uiScale, this.paint);
         canvas.drawLine(cx - radius * 0.25F, cy, cx + radius * 0.25F, cy, this.paint);
         this.paint.setStrokeCap(Cap.BUTT);
         this.drawCenteredFittedText(canvas, "道具" + (slotIndex + 1), cx, bounds.top - 7.0F * this.uiScale, bounds.width() + 22.0F * this.uiScale, 14.0F * this.uiScale, Color.rgb(204, 212, 216));
      } else {
         this.drawPowerUpSymbol(canvas, stored, cx, cy, radius * 0.48F, Color.rgb(14, 18, 22));
         this.drawCenteredFittedText(canvas, stored.getBadge(), cx, bounds.top - 7.0F * this.uiScale, bounds.width() + 22.0F * this.uiScale, 15.0F * this.uiScale, Color.rgb(230, 236, 238));
      }

   }

   private void drawBoostSymbol(Canvas canvas, float cx, float cy, float size, int color) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(color);
      this.scenePath.reset();
      this.scenePath.moveTo(cx + size * 0.12F, cy - size);
      this.scenePath.lineTo(cx - size * 0.58F, cy + size * 0.1F);
      this.scenePath.lineTo(cx - size * 0.08F, cy + size * 0.1F);
      this.scenePath.lineTo(cx - size * 0.32F, cy + size);
      this.scenePath.lineTo(cx + size * 0.62F, cy - size * 0.25F);
      this.scenePath.lineTo(cx + size * 0.08F, cy - size * 0.25F);
      this.scenePath.close();
      canvas.drawPath(this.scenePath, this.paint);
   }

   private void drawPowerUpSymbol(Canvas canvas, PowerUp.Type type, float cx, float cy, float size, int color) {
      this.iconRenderer.drawPowerSymbol(canvas, type, cx, cy, size, color);
   }

   private void drawJoystick(Canvas canvas) {
      if (this.appScreen == GameView.AppScreen.PLAYING && this.state.isPlaying()) {
         PointF base = this.joystick.getBase();
         PointF knob = this.joystick.getKnob();
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(76, 255, 255, 255));
         canvas.drawCircle(base.x, base.y, this.joystick.getMaxDistance(), this.paint);
         this.paint.setColor(Color.argb(45, 0, 0, 0));
         canvas.drawCircle(base.x, base.y, this.joystick.getMaxDistance() * 0.72F, this.paint);
         this.paint.setColor(Color.argb(174, 255, 255, 255));
         canvas.drawCircle(knob.x, knob.y, this.joystick.getMaxDistance() * 0.43F, this.paint);
         this.paint.setColor(Color.rgb(38, 111, 190));
         canvas.drawCircle(knob.x, knob.y, this.joystick.getMaxDistance() * 0.19F, this.paint);
      }
   }

   private void drawOverlay(Canvas canvas) {
      if (this.appScreen == GameView.AppScreen.PLAYING && !this.state.isPlaying()) {
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(176, 6, 9, 13));
         canvas.drawRect(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight(), this.paint);
         RectF panel = new RectF((float)this.getWidth() * 0.5F - Math.min(420.0F * this.uiScale, (float)this.getWidth() * 0.42F), (float)this.getHeight() * 0.26F, (float)this.getWidth() * 0.5F + Math.min(420.0F * this.uiScale, (float)this.getWidth() * 0.42F), (float)this.getHeight() * 0.76F);
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(Color.argb(220, 13, 18, 24));
         canvas.drawRoundRect(panel, 10.0F * this.uiScale, 10.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth(2.0F * this.uiScale);
         this.paint.setColor(Color.argb(135, 238, 240, 232));
         canvas.drawRoundRect(panel, 10.0F * this.uiScale, 10.0F * this.uiScale, this.paint);
         float emblemY = panel.top + 50.0F * this.uiScale;
         this.paint.setStyle(Style.FILL);
         this.paint.setColor(this.overlayTitleColor());
         if (this.state.isCleared()) {
            this.drawStar(canvas, panel.centerX(), emblemY, 23.0F * this.uiScale, true, this.overlayTitleColor());
         } else if (this.state.isFailed()) {
            canvas.drawCircle(panel.centerX(), emblemY, 24.0F * this.uiScale, this.paint);
            this.drawCenteredFittedText(canvas, "!", panel.centerX(), emblemY + 10.0F * this.uiScale, 34.0F * this.uiScale, 30.0F * this.uiScale, Color.rgb(13, 18, 24));
         } else {
            this.drawIconButton(canvas, new RectF(panel.centerX() - 27.0F * this.uiScale, emblemY - 27.0F * this.uiScale, panel.centerX() + 27.0F * this.uiScale, emblemY + 27.0F * this.uiScale), "play");
         }

         this.drawCenteredFittedText(canvas, this.overlayTitle(), panel.centerX(), panel.top + 128.0F * this.uiScale, panel.width() - 42.0F * this.uiScale, 48.0F * this.uiScale, this.overlayTitleColor());
         this.drawCenteredFittedText(canvas, this.overlaySubtitle(), panel.centerX(), panel.top + 178.0F * this.uiScale, panel.width() - 54.0F * this.uiScale, 23.0F * this.uiScale, -1);
         if (this.state.isCleared()) {
            this.drawResultSummary(canvas, panel);
         } else {
            this.drawCenteredFittedText(canvas, this.overlayHint(), panel.centerX(), panel.top + 220.0F * this.uiScale, panel.width() - 64.0F * this.uiScale, 18.0F * this.uiScale, Color.rgb(214, 222, 225));
         }

         this.drawButton(canvas, this.overlayActionButton, this.overlayActionLabel());
         this.paint.setTextAlign(Align.LEFT);
      }
   }

   private int overlayTitleColor() {
      if (this.state.isCleared()) {
         return Color.rgb(213, 168, 79);
      } else {
         return this.state.isFailed() ? Color.rgb(235, 88, 80) : -1;
      }
   }

   private void drawResultSummary(Canvas canvas, RectF panel) {
      float y = panel.top + 200.0F * this.uiScale;
      float starStart = panel.centerX() - 32.0F * this.uiScale;

      for(int i = 0; i < 3; ++i) {
         this.drawStar(canvas, starStart + (float)i * 32.0F * this.uiScale, y - 9.0F * this.uiScale, 13.0F * this.uiScale, i < this.levelResult.getStars(), Color.rgb(213, 168, 79));
      }

      String time = this.formatTime(this.levelResult.getElapsedSeconds());
      String line1 = "用时 " + time + " · 展品 " + this.levelResult.getCollectedTreasures() + "/" + this.levelResult.getTotalTreasures() + " · 奖励金币 +" + this.levelResult.getRewardCoins();
      String line2 = "本局金币 +" + this.runCoinTotal + " · 警戒 " + this.levelResult.getAlerts() + " · 道具 " + this.levelResult.getUsedPowerUps() + (this.levelResult.isNewRecord() ? " · 新纪录" : "");
      String line3 = "最佳潜行连携 x" + this.levelResult.getBestStealthChain() + " · 峰值警戒 " + Math.round(this.levelResult.getPeakThreat() * 100.0F) + "%";
      this.drawCenteredFittedText(canvas, line1, panel.centerX(), y + 25.0F * this.uiScale, panel.width() - 64.0F * this.uiScale, 17.0F * this.uiScale, Color.rgb(238, 240, 232));
      this.drawCenteredFittedText(canvas, line2, panel.centerX(), y + 48.0F * this.uiScale, panel.width() - 64.0F * this.uiScale, 16.0F * this.uiScale, Color.rgb(214, 222, 225));
      this.drawCenteredFittedText(canvas, line3, panel.centerX(), y + 70.0F * this.uiScale, panel.width() - 64.0F * this.uiScale, 15.0F * this.uiScale, Color.rgb(246, 210, 105));
   }

   private String overlayTitle() {
      if (this.state.isCleared()) {
         return this.levelManager.hasNextLevel() ? "展厅清空" : "盗宝完成";
      } else {
         return this.state.isFailed() ? "行动暴露" : "继续";
      }
   }

   private String overlaySubtitle() {
      if (this.state.isCleared()) {
         return this.levelManager.hasNextLevel() ? "更深处的展厅入口已经开启。" : "当前行动路线已全部完成。";
      } else {
         return this.state.isFailed() ? this.state.getFailureReason() : this.getLevel().getTitle();
      }
   }

   private String overlayHint() {
      if (this.state.isCleared()) {
         return "1 星撤离，2 星拿齐展品，3 星限时且无警戒。";
      } else {
         return this.state.isFailed() ? "换一条路线，或者先用道具骗过安保。" : "巡逻、镜头和激光已经冻结。";
      }
   }

   private String formatTime(float seconds) {
      int safeSeconds = Math.max(0, Math.round(seconds));
      int minutes = safeSeconds / 60;
      int remain = safeSeconds % 60;
      return minutes + ":" + (remain < 10 ? "0" : "") + remain;
   }

   private String overlayActionLabel() {
      if (this.state.isCleared()) {
         return this.levelManager.hasNextLevel() ? "下一关" : "再来一次";
      } else {
         return this.state.isFailed() ? "重新开始" : "继续";
      }
   }

   private void drawScreenVignette(Canvas canvas) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(Color.argb(62, 0, 0, 0));
      canvas.drawRect(0.0F, 0.0F, (float)this.getWidth(), 10.0F * this.uiScale, this.paint);
      canvas.drawRect(0.0F, (float)this.getHeight() - 14.0F * this.uiScale, (float)this.getWidth(), (float)this.getHeight(), this.paint);
      canvas.drawRect(0.0F, 0.0F, 9.0F * this.uiScale, (float)this.getHeight(), this.paint);
      canvas.drawRect((float)this.getWidth() - 9.0F * this.uiScale, 0.0F, (float)this.getWidth(), (float)this.getHeight(), this.paint);
   }

   private void drawThreatVignette(Canvas canvas) {
      float threat = this.stealthTracker.getDisplayedThreat();
      if (this.appScreen == GameView.AppScreen.PLAYING && !(threat < 0.18F)) {
         boolean danger = this.stealthTracker.getThreatBand() == ThreatBand.DANGER;
         float pulse = this.reduceMotion ? 0.72F : 0.62F + 0.25F * (float)Math.sin((double)(this.uiElapsedSeconds * 6.0F));
         int alpha = (int)((danger ? 104.0F : 62.0F) * pulse * this.clamp(threat, 0.25F, 1.0F));
         this.paint.setStyle(Style.STROKE);
         this.paint.setStrokeWidth((danger ? 13.0F : 8.0F) * this.uiScale);
         this.paint.setColor(danger ? Color.argb(alpha, 240, 70, 62) : Color.argb(alpha, 241, 184, 83));
         canvas.drawRect(3.0F * this.uiScale, 3.0F * this.uiScale, (float)this.getWidth() - 3.0F * this.uiScale, (float)this.getHeight() - 3.0F * this.uiScale, this.paint);
         this.paint.setStyle(Style.FILL);
      }
   }

   public void drawCenteredFittedText(Canvas canvas, String text, float centerX, float baseline, float maxWidth, float desiredSize, int color) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(color);
      this.paint.setTextAlign(Align.CENTER);
      float accessibleSize = desiredSize * (this.largeTextMode ? 1.12F : 1.0F);
      this.paint.setTextSize(this.fitTextSize(text, maxWidth, accessibleSize));
      canvas.drawText(text, centerX, baseline, this.paint);
      this.paint.setTextAlign(Align.LEFT);
   }

   public void drawLeftFittedText(Canvas canvas, String text, float left, float baseline, float maxWidth, float desiredSize, int color) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(color);
      this.paint.setTextAlign(Align.LEFT);
      float accessibleSize = desiredSize * (this.largeTextMode ? 1.12F : 1.0F);
      this.paint.setTextSize(this.fitTextSize(text, maxWidth, accessibleSize));
      canvas.drawText(text, left, baseline, this.paint);
   }

   private void drawWorldCenteredText(Canvas canvas, String text, float centerX, float baseline, float maxWidth, float desiredSize, int color) {
      this.paint.setStyle(Style.FILL);
      this.paint.setColor(color);
      this.paint.setTextAlign(Align.CENTER);
      this.paint.setTextSize(this.fitWorldTextSize(text, maxWidth, desiredSize));
      canvas.drawText(text, centerX, baseline, this.paint);
      this.paint.setTextAlign(Align.LEFT);
   }

   private void drawStar(Canvas canvas, float centerX, float centerY, float radius, boolean filled, int color) {
      Path star = this.scratchStarPath;
      star.reset();

      for(int i = 0; i < 10; ++i) {
         double angle = (-Math.PI / 2D) + (double)i * Math.PI / (double)5.0F;
         float pointRadius = i % 2 == 0 ? radius : radius * 0.46F;
         float x = centerX + (float)Math.cos(angle) * pointRadius;
         float y = centerY + (float)Math.sin(angle) * pointRadius;
         if (i == 0) {
            star.moveTo(x, y);
         } else {
            star.lineTo(x, y);
         }
      }

      star.close();
      this.paint.setStyle(filled ? Style.FILL : Style.STROKE);
      this.paint.setStrokeWidth(Math.max(1.5F, radius * 0.18F));
      this.paint.setColor(filled ? color : Color.argb(130, Color.red(color), Color.green(color), Color.blue(color)));
      canvas.drawPath(star, this.paint);
      this.paint.setStyle(Style.FILL);
   }

   private int powerUpColor(PowerUp.Type type) {
      if (type == Type.CLOAK) {
         return Color.rgb(104, 196, 202);
      } else if (type == Type.PHASE) {
         return Color.rgb(156, 111, 202);
      } else if (type == Type.SPEED) {
         return Color.rgb(126, 204, 104);
      } else if (type == Type.JAMMER) {
         return Color.rgb(92, 178, 210);
      } else {
         return type == Type.DECOY ? Color.rgb(238, 171, 68) : Color.rgb(226, 194, 75);
      }
   }

   private PowerUp.Type[] shopTypes() {
      return SHOP_TYPES;
   }

   private int shopPrice(PowerUp.Type type) {
      if (type == Type.CLOAK) {
         return 120;
      } else if (type == Type.PHASE) {
         return 150;
      } else if (type == Type.SPEED) {
         return 95;
      } else {
         return type == Type.JAMMER ? 180 : 70;
      }
   }

   private int upgradePrice(UpgradeType type) {
      int level = this.upgradeLevels[type.ordinal()];
      int base;
      if (type == UpgradeType.SHOES) {
         base = 180;
      } else if (type == UpgradeType.GLOVES) {
         base = 200;
      } else if (type == UpgradeType.BATTERY) {
         base = 190;
      } else {
         base = 170;
      }

      return base + level * 120;
   }

   private String shopDescription(PowerUp.Type type) {
      if (type == Type.CLOAK) {
         return "短暂避开视线锁定";
      } else if (type == Type.PHASE) {
         return "三秒穿越墙体";
      } else if (type == Type.SPEED) {
         return "五秒双倍移动";
      } else {
         return type == Type.JAMMER ? "部署八秒范围干扰" : "吸引安保注意";
      }
   }

   private int getShopStockTotal() {
      int total = 0;

      for(int count : this.shopStock) {
         total += count;
      }

      return total;
   }

   private void drawWrappedCenteredText(Canvas canvas, String text, float centerX, float top, float maxWidth, float desiredSize, float lineHeight, int maxLines, int color) {
      if (text != null && text.length() != 0 && maxLines > 0) {
         this.paint.setStyle(Style.FILL);
         this.paint.setTextAlign(Align.CENTER);
         this.paint.setTextSize(desiredSize);
         this.paint.setColor(color);
         Paint.FontMetrics metrics = this.paint.getFontMetrics();
         float baseline = top - metrics.ascent;
         int start = 0;

         for(int lineIndex = 0; lineIndex < maxLines && start < text.length(); ++lineIndex) {
            String remaining = text.substring(start);
            int count = Math.max(1, this.paint.breakText(remaining, true, Math.max(1.0F, maxWidth), (float[])null));
            count = Math.min(count, remaining.length());
            boolean truncated = start + count < text.length() && lineIndex == maxLines - 1;
            String line = remaining.substring(0, count).trim();
            if (truncated) {
               while(line.length() > 0 && this.paint.measureText(line + "…") > maxWidth) {
                  line = line.substring(0, line.length() - 1);
               }

               line = line + "…";
            }

            canvas.drawText(line, centerX, baseline + (float)lineIndex * lineHeight, this.paint);
            start += count;
         }

         this.paint.setTextAlign(Align.LEFT);
      }
   }

   private float fitTextSize(String text, float maxWidth, float desiredSize) {
      float safeMaxWidth = Math.max(maxWidth, 1.0F);
      this.paint.setTextSize(desiredSize);
      float textWidth = this.paint.measureText(text);
      return textWidth <= safeMaxWidth ? desiredSize : Math.max(12.0F * this.uiScale, desiredSize * safeMaxWidth / Math.max(textWidth, 1.0F));
   }

   private float fitWorldTextSize(String text, float maxWidth, float desiredSize) {
      float safeMaxWidth = Math.max(maxWidth, 1.0F);
      this.paint.setTextSize(desiredSize);
      float textWidth = this.paint.measureText(text);
      return textWidth <= safeMaxWidth ? desiredSize : Math.max(10.0F, desiredSize * safeMaxWidth / Math.max(textWidth, 1.0F));
   }

   private int withAlpha(int color, int alpha) {
      int safeAlpha = Math.max(0, Math.min(255, alpha));
      return Color.argb(safeAlpha, Color.red(color), Color.green(color), Color.blue(color));
   }

   private float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   private Level getLevel() {
      return this.levelManager.getCurrentLevel();
   }

   static {
      SHOP_TYPES = new PowerUp.Type[]{Type.CLOAK, Type.PHASE, Type.SPEED, Type.JAMMER, Type.DECOY};
   }

   private static enum AppScreen {
      TITLE,
      CHARACTER_SELECT,
      LEVEL_SELECT,
      SHOP,
      SETTINGS,
      PLAYING;

      // $FF: synthetic method
      private static AppScreen[] $values() {
         return new AppScreen[]{TITLE, CHARACTER_SELECT, LEVEL_SELECT, SHOP, SETTINGS, PLAYING};
      }
   }
}
