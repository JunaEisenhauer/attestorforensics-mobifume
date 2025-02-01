package com.attestorforensics.mobifumecore.controller.updater;

import com.attestorforensics.mobifumecore.model.event.update.UpdatingEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class UpdatingListener implements Listener {

  private final UpdateController updateController;

  private UpdatingListener(UpdateController updateController) {
    this.updateController = updateController;
  }

  static UpdatingListener create(UpdateController updateController) {
    return new UpdatingListener(updateController);
  }

  @EventHandler
  public void onUpdating(UpdatingEvent event) {
    updateController.setState(event.getState());
  }
}
