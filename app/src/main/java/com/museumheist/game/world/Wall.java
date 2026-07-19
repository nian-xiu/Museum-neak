package com.museumheist.game.world;
import android.graphics.RectF;
public class Wall { private final RectF bounds; private final boolean blocking; public Wall(float l,float t,float r,float b){this(new RectF(l,t,r,b),true);} public Wall(RectF b){this(b,true);} public Wall(RectF b,boolean blocking){bounds=new RectF(b);this.blocking=blocking;} public RectF getBounds(){return bounds;} public boolean isBlocking(){return blocking;} }
