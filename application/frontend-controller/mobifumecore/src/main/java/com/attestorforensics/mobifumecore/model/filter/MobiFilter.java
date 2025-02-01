package com.attestorforensics.mobifumecore.model.filter;

import com.attestorforensics.mobifumecore.model.setting.Evaporant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MobiFilter implements Filter, Serializable {

  private static final transient long WARNING_TIME = 1000L * 60 * 60 * 24 * 30 * 11; // ~ 11 months
  private static final transient long OUT_OF_TIME = 1000L * 60 * 60 * 24 * 365; // ~ 12 months
  private final String id;
  private final long date;
  private transient FilterFileHandler fileHandler;
  private final List<RunProcess> runs = new ArrayList<>();
  private boolean removed;

  public MobiFilter(FilterFileHandler fileHandler, String id) {
    this.fileHandler = fileHandler;
    this.id = id;
    date = System.currentTimeMillis();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public long getAddedDate() {
    return date;
  }

  @Override
  public double getPercentage() {
    double percentage = runs.stream().mapToDouble(Run::getPercentage).sum();
    if (percentage > 1) {
      percentage = 1;
    }
    return percentage;
  }

  @Override
  public int getApproximateUsagesLeft() {
    double percentage = getPercentage();
    int usages = runs.size();
    if (percentage == 0) {
      return -1;
    }

    if (percentage - usages == 0) {
      return 0;
    }

    return (int) (usages / percentage - usages);
  }

  @Override
  public List<Run> getRuns() {
    return runs.stream().map(Run.class::cast).collect(Collectors.toList());
  }

  @Override
  public void addRun(int cycle, Evaporant evaporant, double evaporantAmount, int totalFilterCount) {
    RunProcess run = new RunProcess(cycle, System.currentTimeMillis(), evaporant, evaporantAmount,
        totalFilterCount, false);
    runs.add(run);
    fileHandler.saveFilter(this);
  }

  @Override
  public boolean isPercentageWarning() {
    return getPercentage() >= 0.9;
  }

  @Override
  public boolean isTimeWarning() {
    long lifeTime = (System.currentTimeMillis() - date);
    return lifeTime > WARNING_TIME;
  }

  @Override
  public boolean isOutOfTime() {
    long lifeTime = System.currentTimeMillis() - date;
    return lifeTime > OUT_OF_TIME;
  }

  @Override
  public boolean isUsable() {
    if (getPercentage() == 1) {
      return false;
    }
    if (isOutOfTime()) {
      long outOfTimeDate = date + OUT_OF_TIME;
      return getRuns().stream().noneMatch(run -> run.getDate() > outOfTimeDate);
    }
    return true;
  }

  @Override
  public boolean isRemoved() {
    return removed;
  }

  @Override
  public void setRemoved() {
    removed = true;
    fileHandler.saveFilter(this);
  }

  public void setFileHandler(FilterFileHandler fileHandler) {
    this.fileHandler = fileHandler;
  }
}
