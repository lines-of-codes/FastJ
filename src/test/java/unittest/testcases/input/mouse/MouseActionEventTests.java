package unittest.testcases.input.mouse;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.display.FastJCanvas;

import tech.fastj.input.mouse.MouseActionListener;
import tech.fastj.input.mouse.MouseScrollType;
import tech.fastj.input.mouse.events.MouseButtonEvent;
import tech.fastj.input.mouse.events.MouseMotionEvent;
import tech.fastj.input.mouse.events.MouseScrollEvent;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import unittest.EnvironmentHelper;
import unittest.mock.systems.control.MockConfigurableScene;
import unittest.mock.systems.control.MockSceneManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MouseActionEventTests {

    @BeforeAll
    public static void onlyRunIfNotHeadless() {
        assumeFalse(EnvironmentHelper.IsEnvironmentHeadless);
    }

    @Test
    @Order(0)
    void checkMouseActionListener_MouseButtonEvents() throws AWTException {
        AtomicBoolean didMousePress = new AtomicBoolean();
        AtomicBoolean didMouseRelease = new AtomicBoolean();
        AtomicBoolean didMouseClick = new AtomicBoolean();
        AtomicBoolean wasMouseButton1Pressed = new AtomicBoolean();
        AtomicBoolean wasMouseButton1Released = new AtomicBoolean();
        AtomicBoolean wasMouseButton1Clicked = new AtomicBoolean();
        AtomicInteger mouseButtonClickCount = new AtomicInteger();
        AtomicBoolean hasUpdated = new AtomicBoolean();
        int expectedClickCount = 1;
        Robot robot = new Robot();

        MockConfigurableScene mockConfigurableScene = new MockConfigurableScene(
                (canvas, scene) -> {
                    // wait for canvas to show up on screen
                    while (!canvas.getRawCanvas().isShowing()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // initial focus + robot mouse positioning
                    if (!canvas.getRawCanvas().hasFocus()) {
                        canvas.getRawCanvas().requestFocus();
                    }
                    Point canvasLocationOnScreen = new Point(canvas.getRawCanvas().getLocationOnScreen()).add(
                            (int) canvas.getCanvasCenter().x,
                            (int) canvas.getCanvasCenter().y
                    );
                    robot.mouseMove(canvasLocationOnScreen.x, canvasLocationOnScreen.y);
                    Log.info(MouseActionEventTests.class, "Mouse pointer located at {}", MouseInfo.getPointerInfo().getLocation());

                    scene.inputManager.addMouseActionListener(new MouseActionListener() {
                        @Override
                        public void onMousePressed(MouseButtonEvent mouseButtonEvent) {
                            Log.info(MouseActionEventTests.class, "press {}", mouseButtonEvent);
                            didMousePress.set(true);
                            if (mouseButtonEvent.getMouseButton() == MouseEvent.BUTTON1) {
                                wasMouseButton1Pressed.set(true);
                            }
                        }

                        @Override
                        public void onMouseReleased(MouseButtonEvent mouseButtonEvent) {
                            Log.info(MouseActionEventTests.class, "release {}", mouseButtonEvent);
                            didMouseRelease.set(true);
                            if (mouseButtonEvent.getMouseButton() == MouseEvent.BUTTON1) {
                                wasMouseButton1Released.set(true);
                            }
                        }

                        @Override
                        public void onMouseClicked(MouseButtonEvent mouseButtonEvent) {
                            Log.info(MouseActionEventTests.class, "click {}", mouseButtonEvent);
                            didMouseClick.set(true);
                            mouseButtonClickCount.set(mouseButtonEvent.getClickCount());
                            if (mouseButtonEvent.getMouseButton() == MouseEvent.BUTTON1) {
                                wasMouseButton1Clicked.set(true);
                            }
                        }
                    });
                },
                (canvas, scene) -> {
                    if (hasUpdated.get()) {
                        return;
                    }
                    hasUpdated.set(true);

                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.delay(100);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }
        );

        MockSceneManager mockSceneManager = new MockSceneManager(mockConfigurableScene) {
            @Override
            public void render(FastJCanvas display) {
                if (!hasUpdated.get() || !didMousePress.get() || !didMouseRelease.get() || !didMouseClick.get()) {
                    return;
                }

                FastJEngine.closeGame();
            }
        };
        FastJEngine.init("Robot mouse button", mockSceneManager);
        FastJEngine.run();

        assertTrue(wasMouseButton1Pressed.get(), "Mouse button 1 should have been pressed.");
        assertTrue(wasMouseButton1Released.get(), "Mouse button 1 should have been released.");
        assertTrue(wasMouseButton1Clicked.get(), "Mouse button 1 should have been clicked.");
        assertEquals(expectedClickCount, mouseButtonClickCount.get(), "The mouse should have been clicked once.");
    }

    @Test
    @Order(1)
    void checkMouseActionListener_MouseMotionEvents() throws AWTException {
        AtomicBoolean didMouseMove = new AtomicBoolean();
        AtomicBoolean didMouseDrag = new AtomicBoolean();
        AtomicReference<Point> mouseMoveLocation = new AtomicReference<>();
        AtomicReference<Point> mouseDragLocation = new AtomicReference<>();
        Point expectedMouseMove = Point.unit();
        Point expectedMouseDrag = Point.unit().add(Point.right());
        Point screenLocation = Point.origin();
        AtomicBoolean hasUpdated = new AtomicBoolean();
        Robot robot = new Robot();

        MockConfigurableScene mockConfigurableScene = new MockConfigurableScene(
                (canvas, scene) -> {
                    // wait for canvas to show up on screen
                    while (!canvas.getRawCanvas().isShowing()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // initial focus + robot mouse positioning
                    if (!canvas.getRawCanvas().hasFocus()) {
                        canvas.getRawCanvas().requestFocus();
                    }
                    Point canvasLocationOnScreen = new Point(canvas.getRawCanvas().getLocationOnScreen());
                    screenLocation.add(canvasLocationOnScreen);
                    robot.mouseMove(canvasLocationOnScreen.x, canvasLocationOnScreen.y);
                    Log.info(MouseActionEventTests.class, "Mouse pointer located at {}", MouseInfo.getPointerInfo().getLocation());

                    scene.inputManager.addMouseActionListener(new MouseActionListener() {
                        @Override
                        public void onMouseMoved(MouseMotionEvent mouseMotionEvent) {
                            Log.info(MouseActionEventTests.class, "move {}", mouseMotionEvent);
                            didMouseMove.set(true);
                            Pointf mouseLocation = mouseMotionEvent.getMouseLocation();
                            Point mouseLocationTruncated = new Point((int) mouseLocation.x, (int) mouseLocation.y);
                            mouseMoveLocation.set(mouseLocationTruncated);
                        }

                        @Override
                        public void onMouseDragged(MouseMotionEvent mouseMotionEvent) {
                            Log.info(MouseActionEventTests.class, "drag {}", mouseMotionEvent);
                            didMouseDrag.set(true);
                            Pointf mouseLocation = mouseMotionEvent.getMouseLocation();
                            Point mouseLocationTruncated = new Point((int) mouseLocation.x, (int) mouseLocation.y);
                            mouseDragLocation.set(mouseLocationTruncated);
                        }
                    });
                },
                (canvas, scene) -> {
                    if (hasUpdated.get()) {
                        return;
                    }
                    hasUpdated.set(true);

                    robot.mouseMove(expectedMouseMove.x + screenLocation.x, expectedMouseMove.y + screenLocation.y);
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    robot.mouseMove(expectedMouseDrag.x + screenLocation.x, expectedMouseDrag.y + screenLocation.y);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }
        );

        MockSceneManager mockSceneManager = new MockSceneManager(mockConfigurableScene) {
            @Override
            public void render(FastJCanvas display) {
                if (!hasUpdated.get() || !didMouseMove.get() || !didMouseDrag.get()) {
                    return;
                }

                FastJEngine.closeGame();
            }
        };

        FastJEngine.init("Robot mouse button", mockSceneManager);
        FastJEngine.run();

        assertEquals(expectedMouseMove, mouseMoveLocation.get(), "The mouse should have moved to " + expectedMouseMove + ".");
        assertEquals(expectedMouseDrag, mouseDragLocation.get(), "The mouse should have been dragged to " + expectedMouseDrag + ".");
    }

    @Test
    @Order(2)
    void checkMouseActionListener_MouseWheelEvents() throws AWTException {
        AtomicBoolean didMouseScroll = new AtomicBoolean();
        AtomicReference<Double> mouseWheelRotationAmount = new AtomicReference<>();
        AtomicReference<MouseScrollType> mouseScrollType = new AtomicReference<>();
        AtomicBoolean hasUpdated = new AtomicBoolean(false);
        double expectedWheelRotation = 5.0d;
        MouseScrollType expectedScrollType = MouseScrollType.Unit;
        Robot robot = new Robot();

        MockConfigurableScene mockConfigurableScene = new MockConfigurableScene(
                (canvas, scene) -> {
                    // wait for canvas to show up on screen
                    while (!canvas.getRawCanvas().isShowing()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // initial focus + robot mouse positioning
                    if (!canvas.getRawCanvas().hasFocus()) {
                        canvas.getRawCanvas().requestFocus();
                    }
                    Point canvasCenterOnScreen = new Point(canvas.getRawCanvas().getLocationOnScreen()).add(
                            (int) canvas.getCanvasCenter().x,
                            (int) canvas.getCanvasCenter().y
                    );
                    robot.mouseMove(canvasCenterOnScreen.x, canvasCenterOnScreen.y);
                    Log.info(MouseActionEventTests.class, "Mouse pointer located at {}", MouseInfo.getPointerInfo().getLocation());

                    scene.inputManager.addMouseActionListener(new MouseActionListener() {
                        @Override
                        public void onMouseWheelScrolled(MouseScrollEvent mouseScrollEvent) {
                            Log.info(MouseActionEventTests.class, "scroll {} {} {} {} {}", mouseScrollEvent, mouseScrollEvent.getMouseScrollType(), mouseScrollEvent.getRawEvent().getPreciseWheelRotation(), mouseScrollEvent.getRawEvent().getScrollAmount(), mouseScrollEvent.getRawEvent().getUnitsToScroll());
                            didMouseScroll.set(true);
                            mouseWheelRotationAmount.set(mouseScrollEvent.getWheelRotation());
                            mouseScrollType.set(mouseScrollEvent.getMouseScrollType());
                        }
                    });
                },
                (canvas, scene) -> {
                    if (hasUpdated.get()) {
                        return;
                    }
                    hasUpdated.set(true);

                    robot.mouseWheel((int) expectedWheelRotation);
                }
        );

        MockSceneManager mockSceneManager = new MockSceneManager(mockConfigurableScene) {
            @Override
            public void render(FastJCanvas display) {
                if (!hasUpdated.get() || !didMouseScroll.get()) {
                    return;
                }

                FastJEngine.closeGame();
            }
        };
        FastJEngine.init("Robot mouse button", mockSceneManager);
        FastJEngine.run();

        assertEquals(expectedScrollType, mouseScrollType.get(), "The mouse scroll type should have been " + expectedScrollType + ".");
        assertEquals(expectedWheelRotation, mouseWheelRotationAmount.get(), "The mouse wheel should have been rotated " + expectedWheelRotation + ".");
    }
}
