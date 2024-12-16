package gui;

import javax.swing.*;
import java.awt.*;

public class BorderProgressBar extends JPanel {

    private final JComponent targetComponent;
    private int progressPosition = 0;
    private final int STEP_SIZE = 10;

    public BorderProgressBar(JComponent targetComponent) {
        this.targetComponent = targetComponent;
        setOpaque(false);
    }

    // Метод для запуска анимации прогресс-бара
    public void startAnimation() {
        Timer timer = new Timer(20, e -> {
            progressPosition += STEP_SIZE;
            int perimeter = getPerimeter();
            if (progressPosition >= perimeter) {
                progressPosition = 0;
            }
            repaint();
        });
        timer.start();
    }

    // Метод для вычисления периметра компонента
    private int getPerimeter() {
        Rectangle bounds = targetComponent.getBounds();
        int width = bounds.width + 10;
        int height = bounds.height + 10;
        return 2 * (width + height);
    }

    // Метод для отрисовки прогресс-бара
    private void drawProgressBar(Graphics2D g2, int x, int y, int width, int height) {
        int pos = progressPosition;
        if (pos < width) {
            // Верхняя сторона
            g2.fillRect(x + pos, y - 5, 50, 10);
        } else if (pos < width + height) {
            // Правая сторона
            g2.fillRect(x + width - 5, y + (pos - width), 10, 50);
        } else if (pos < 2 * width + height) {
            // Нижняя сторона
            g2.fillRect(x + (width - (pos - (width + height))), y + height - 5, 50, 10);
        } else {
            // Левая сторона
            g2.fillRect(x - 5, y + (height - (pos - (2 * width + height))), 10, 50);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Получение графического контекста и установка сглаживания
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Получение границ целевого компонента
        Rectangle bounds = targetComponent.getBounds();
        int x = bounds.x - 5;
        int y = bounds.y - 5;
        int width = bounds.width + 10;
        int height = bounds.height + 10;

        // Отрисовка прогресс-бара
        g2.setColor(new Color(0x60CF92));
        drawProgressBar(g2, x, y, width, height);
    }
}
