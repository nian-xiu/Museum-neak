package com.museumheist.game.logic;
import android.graphics.PointF; import java.util.*;
public class PatrolPath { private final List<PointF> points=new ArrayList<>(); public PatrolPath(PointF... pts){for(PointF p:pts)points.add(new PointF(p.x,p.y));} public PatrolPath(List<PointF> pts){for(PointF p:pts)points.add(new PointF(p.x,p.y));} public int size(){return points.size();} public PointF get(int i){return points.get(Math.floorMod(i,points.size()));} public List<PointF> getPoints(){return Collections.unmodifiableList(points);} }
