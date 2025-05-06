package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается.
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
@PersistWindowState
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final WindowStateManager windowStateManager;
    private final List<JInternalFrame> closedWindows = new ArrayList<>();

    public MainApplicationFrame() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset*2,
                screenSize.height - inset*2);

        setContentPane(desktopPane);
        windowStateManager = new WindowStateManager(this, desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        windowStateManager.loadWindowStates();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                closedWindows.add(frame);
            }
        });
    }

    private JMenuBar generateMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createFileMenu());
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createRobotMenu());

        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem restoreWindowsItem = new JMenuItem("Восстановить окна", KeyEvent.VK_R);
        restoreWindowsItem.addActionListener(event -> restoreClosedWindows());
        fileMenu.add(restoreWindowsItem);

        JMenuItem exitItem = new JMenuItem("Выход", KeyEvent.VK_X);
        exitItem.addActionListener(event -> confirmExit());
        fileMenu.add(exitItem);

        return fileMenu;
    }

    private JMenu createRobotMenu() {
        JMenu robotMenu = new JMenu("Робот");
        robotMenu.setMnemonic(KeyEvent.VK_R);

        // Размер робота
        JMenu sizeMenu = new JMenu("Размер робота");
        ButtonGroup sizeGroup = new ButtonGroup();

        JRadioButtonMenuItem smallSize = new JRadioButtonMenuItem("Маленький (20)");
        smallSize.addActionListener(e -> getGameWindow().getVisualizer().setRobotSize(20));
        sizeMenu.add(smallSize);
        sizeGroup.add(smallSize);

        JRadioButtonMenuItem mediumSize = new JRadioButtonMenuItem("Средний (40)", true);
        mediumSize.addActionListener(e -> getGameWindow().getVisualizer().setRobotSize(40));
        sizeMenu.add(mediumSize);
        sizeGroup.add(mediumSize);

        JRadioButtonMenuItem largeSize = new JRadioButtonMenuItem("Большой (60)");
        largeSize.addActionListener(e -> getGameWindow().getVisualizer().setRobotSize(60));
        sizeMenu.add(largeSize);
        sizeGroup.add(largeSize);

        robotMenu.add(sizeMenu);

        // Форма робота
        JMenu shapeMenu = new JMenu("Форма");
        ButtonGroup shapeGroup = new ButtonGroup();

        JRadioButtonMenuItem ovalShape = new JRadioButtonMenuItem("Овал", true);
        ovalShape.addActionListener(e -> getGameWindow().getVisualizer().getRobotSettings().setShape(RobotShape.OVAL));
        shapeMenu.add(ovalShape);
        shapeGroup.add(ovalShape);

        JRadioButtonMenuItem rectShape = new JRadioButtonMenuItem("Прямоугольник");
        rectShape.addActionListener(e -> getGameWindow().getVisualizer().getRobotSettings().setShape(RobotShape.RECTANGLE));
        shapeMenu.add(rectShape);
        shapeGroup.add(rectShape);

        JRadioButtonMenuItem triangleShape = new JRadioButtonMenuItem("Треугольник");
        triangleShape.addActionListener(e -> getGameWindow().getVisualizer().getRobotSettings().setShape(RobotShape.TRIANGLE));
        shapeMenu.add(triangleShape);
        shapeGroup.add(triangleShape);

        robotMenu.add(shapeMenu);

        // Цвет робота
        JMenu colorMenu = new JMenu("Цвет");
        JMenuItem magentaColor = new JMenuItem("Пурпурный");
        magentaColor.addActionListener(e -> getGameWindow().getVisualizer().getRobotSettings().setRobotColor(Color.MAGENTA));
        colorMenu.add(magentaColor);

        JMenuItem blueColor = new JMenuItem("Синий");
        blueColor.addActionListener(e -> getGameWindow().getVisualizer().getRobotSettings().setRobotColor(Color.BLUE));
        colorMenu.add(blueColor);

        JMenuItem redColor = new JMenuItem("Красный");
        redColor.addActionListener(e -> getGameWindow().getVisualizer().getRobotSettings().setRobotColor(Color.RED));
        colorMenu.add(redColor);

        JMenuItem customColor = new JMenuItem("Выбрать цвет...");
        customColor.addActionListener(e -> {
            JColorChooser colorChooser = new JColorChooser();
            Color newColor = JColorChooser.showDialog(
                    MainApplicationFrame.this,
                    "Выберите цвет робота",
                    getGameWindow().getVisualizer().getRobotSettings().getRobotColor()
            );
            if (newColor != null) {
                getGameWindow().getVisualizer().getRobotSettings().setRobotColor(newColor);
            }
        });
        colorMenu.add(customColor);

        robotMenu.add(colorMenu);

        // Скорость робота
        JMenu speedMenu = new JMenu("Скорость");
        ButtonGroup speedGroup = new ButtonGroup();

        JRadioButtonMenuItem slowSpeed = new JRadioButtonMenuItem("Медленная");
        slowSpeed.addActionListener(e -> {
            RobotSettings settings = getGameWindow().getVisualizer().getRobotSettings();
            settings.setMaxVelocity(0.05);
            settings.setMaxAngularVelocity(0.0005);
        });
        speedMenu.add(slowSpeed);
        speedGroup.add(slowSpeed);

        JRadioButtonMenuItem normalSpeed = new JRadioButtonMenuItem("Обычная", true);
        normalSpeed.addActionListener(e -> {
            RobotSettings settings = getGameWindow().getVisualizer().getRobotSettings();
            settings.setMaxVelocity(0.1);
            settings.setMaxAngularVelocity(0.001);
        });
        speedMenu.add(normalSpeed);
        speedGroup.add(normalSpeed);

        JRadioButtonMenuItem fastSpeed = new JRadioButtonMenuItem("Быстрая");
        fastSpeed.addActionListener(e -> {
            RobotSettings settings = getGameWindow().getVisualizer().getRobotSettings();
            settings.setMaxVelocity(0.2);
            settings.setMaxAngularVelocity(0.002);
        });
        speedMenu.add(fastSpeed);
        speedGroup.add(fastSpeed);

        robotMenu.add(speedMenu);

        return robotMenu;
    }

    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        lookAndFeelMenu.add(createLookAndFeelItem("Системная схема", UIManager.getSystemLookAndFeelClassName()));
        lookAndFeelMenu.add(createLookAndFeelItem("Универсальная схема", UIManager.getCrossPlatformLookAndFeelClassName()));

        return lookAndFeelMenu;
    }

    private JMenuItem createLookAndFeelItem(String name, String lookAndFeelClass) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(event -> {
            setLookAndFeel(lookAndFeelClass);
            this.invalidate();
        });
        return item;
    }

    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription("Тестовые команды");

        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener(event -> Logger.debug("Новая строка"));
        testMenu.add(addLogMessageItem);

        return testMenu;
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }

    private void confirmExit() {
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");

        int confirmed = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите выйти?", "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION);
        if (confirmed == JOptionPane.YES_OPTION) {
            windowStateManager.saveWindowStates();
            System.exit(0);
        }
    }

    private void restoreClosedWindows() {
        if (closedWindows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Нет закрытых окон для восстановления",
                    "Информация",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<JInternalFrame> windowsToRestore = new ArrayList<>(closedWindows);
        closedWindows.clear();

        for (JInternalFrame frame : windowsToRestore) {
            try {
                if (frame instanceof LogWindow) {
                    LogWindow logWindow = createLogWindow();
                    addWindow(logWindow);
                } else if (frame instanceof GameWindow) {
                    GameWindow gameWindow = new GameWindow();
                    gameWindow.setSize(400, 400);
                    addWindow(gameWindow);
                }
            } catch (Exception e) {
                Logger.error("Ошибка при восстановлении окна: " + e.getMessage());
            }
        }
    }

    private GameWindow getGameWindow() {
        for (Component comp : desktopPane.getComponents()) {
            if (comp instanceof GameWindow) {
                return (GameWindow) comp;
            }
        }
        return null;
    }
}
