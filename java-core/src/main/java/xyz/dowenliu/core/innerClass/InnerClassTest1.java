package xyz.dowenliu.core.innerClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * <p>create at 2019/11/1</p>
 *
 * @author Cay Horstmann
 * @author liufl
 */
public class InnerClassTest1 {
    public static void main(String[] args) {
        TalkingClock clock = new TalkingClock(1000, false);
        TalkingClock listerProvider = new TalkingClock(0, true);
        TalkingClock.TimePrinter listener = listerProvider.new TimePrinter();
        clock.start(listener);

        // keep program running until user selects "OK"
        JOptionPane.showMessageDialog(null, "Quit program?");
        System.exit(0);
    }
}

/**
 * A clock that prints the time in regular intervals.
 */
class TalkingClock {
    private int interval;
    private boolean beep;

    TalkingClock(int interval, boolean beep) {
        this.interval = interval;
        this.beep = beep;
    }

    void start(ActionListener listener) {
        Timer t = new Timer(interval, listener);
        t.start();
    }

    public class TimePrinter implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("At the tone, the time is " + new Date());
            if (beep) Toolkit.getDefaultToolkit().beep();
        }
    }
}
