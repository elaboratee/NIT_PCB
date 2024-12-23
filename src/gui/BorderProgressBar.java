package gui;

import javax.swing.*;
import java.awt.*;

public class BorderProgressBar extends JPanel {

    private int progressPosition = 0;
    private final int STEP_SIZE = 10;
    private final Rectangle targetBounds;

    public BorderProgressBar(Rectangle targetBounds) {
        this.targetBounds = targetBounds;
        setOpaque(false);
    }

    /**
     * Метод для запуска анимации прогресс-бара
     */
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

    /**
     * Метод для вычисления периметра компонента
     *
     * @return периметр компонента
     */
    private int getPerimeter() {
        int width = targetBounds.width + 10;
        int height = targetBounds.height + 10;
        return 2 * (width + height);
    }

    /**
     * Метод для отрисовки прогресс-бара
     *
     * @param g2     графический контекст компонента
     * @param x      граница целевого компонента по оси X
     * @param y      граница целевого компонента по оси Y
     * @param width  ширина целевого компонента
     * @param height высота целевого компонента
     */
    private void drawProgressBar(Graphics2D g2,
                                 int x, int y,
                                 int width, int height) {
        int pos = progressPosition;
        if (pos < width) {
            // Верхняя сторона
            g2.fillRect(x + pos, y, 50, 10);
        } else if (pos < width + height) {
            // Правая сторона
            g2.fillRect(x + width, y + (pos - width), 10, 50);
        } else if (pos < 2 * width + height) {
            // Нижняя сторона
            g2.fillRect(x + (width - (pos - (width + height))), y + height, 50, 10);
        } else {
            // Левая сторона
            g2.fillRect(x, y + (height - (pos - (2 * width + height))), 10, 50);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Получение графического контекста и установка сглаживания
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Получение границ целевого компонента
        int x = targetBounds.x - 10;
        int y = targetBounds.y - 10;
        int width = targetBounds.width + 10;
        int height = targetBounds.height + 10;

        // Отрисовка прогресс-бара
        g2.setColor(new Color(0x60CF92));
        drawProgressBar(g2, x, y, width, height);
    }
}
