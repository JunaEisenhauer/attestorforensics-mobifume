package com.attestorforensics.mobifumecore.model.listener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.commons.compress.utils.Lists;

/**
 * Responsible for listener registration and event calling.
 */
public class EventDispatcher implements ListenerRegistry, EventCaller {

  private final ExecutorService executorService;
  private final List<ListenerMethod> listenerMethods = Lists.newArrayList();

  private EventDispatcher(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public static EventDispatcher create(ExecutorService executorService) {
    return new EventDispatcher(executorService);
  }


  @Override
  public void registerListener(Listener listener) {
    for (Method method : listener.getClass().getDeclaredMethods()) {
      tryRegisterListenerMethod(listener, method);
    }
  }

  @Override
  public void unregisterListener(Listener listener) {
    listenerMethods.removeIf(listenerMethod -> listenerMethod.getListener() == listener);
  }

  @Override
  public void call(Event event) {
    executorService.execute(() -> executeEventCall(event));
  }

  /**
   * Tries to parse the method and register the listener method.
   *
   * @param listener the listener of the method
   * @param method   the method to try to register
   */
  private void tryRegisterListenerMethod(Listener listener, Method method) {
    EventHandler eventHandler = method.getDeclaredAnnotation(EventHandler.class);
    // check if method is annotated with EventHandler
    if (eventHandler == null) {
      return;
    }

    Class<?>[] parameters = method.getParameterTypes();
    // check if method has exactly one parameter
    if (parameters.length != 1) {
      return;
    }

    Class<?> eventType = parameters[0];
    // check if class of parameter implements Event
    if (!Event.class.isAssignableFrom(eventType)) {
      return;
    }

    listenerMethods.add(ListenerMethod.create(listener, eventType, method));
  }

  private void executeEventCall(Event event) {
    listenerMethods.stream()
        .filter(listenerMethod -> listenerMethod.getEventType() == event.getClass())
        .forEach(listenerMethod -> invokeListenerMethod(listenerMethod, event));
  }

  private void invokeListenerMethod(ListenerMethod listenerMethod, Event event) {
    try {
      listenerMethod.getMethod().invoke(listenerMethod.getListener(), event);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
