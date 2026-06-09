/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.kelsz.esla;

/**
 *
 * @author kelsz-dev
 */
import com.formdev.flatlaf.FlatLightLaf;
import com.kelsz.esla.features.auth.Login;
import ui.style;

public class Esla {

   public static void main(String[] args) {

        try {
            FlatLightLaf.setup(); // 🌟 global theme
            style.applyGlobalDialogStyle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Login().setVisible(true);
    }
}
