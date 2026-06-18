package org.example.utils;

import javax.swing.*;

/**
 * 危险操作确认弹窗程序
 */
public class DialogUtil {
    /**
     * 弹出危险操作确认框
     *
     * @param options       弹框选项
     * @param title         标题
     * @param message       描述
     * @param defaultSelect 默认选中
     * @return 弹框选择值
     */
    public static Object showDangerConfirmDialog(Object[] options, String title, String message, Object defaultSelect) {
        int result = JOptionPane.showOptionDialog(null, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                options, options[0]);
        return result >= options.length ? defaultSelect : options[result];
    }
}
