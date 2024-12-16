import com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme;
import gui.MainScreen;
import gui.SplashScreen;
import org.opencv.core.Core;

import javax.swing.*;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class DefectDetectionApp {

    private static final CountDownLatch latch = new CountDownLatch(1);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FlatGradiantoNatureGreenIJTheme.setup();
    }

    public static void main(String[] args) {
        // Отображение сплэш-скрина
        SwingUtilities.invokeLater(DefectDetectionApp::showSplashScreen);

        // Блокировка основного потока
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Отображение основного окна
        SwingUtilities.invokeLater(MainScreen::showMainScreen);
    }

    // Метод для отображения сплэш-скрина с задержкой
    private static void showSplashScreen() {
        SplashScreen.showSplashScreen();
        Random rand = new Random(System.currentTimeMillis());
        Timer timer = new Timer(rand.nextInt(3000, 7000), e -> {
            SplashScreen.closeSplashScreen();
            latch.countDown();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
