/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vpb.tts.constant;

/**
 *
 * @author quynx
 */
public class ColOrderConstant {
    
    public static int ColOrderConstant (String tablename) {
      switch (tablename){
          case "SpotQuotes":
                 return 0;
          case "BondsQuotes":
              return 1;
          case "FxSwapRevalPoints":
              return 2;
          case "FloatingRatesValues":
              return 3;
          case "CurvesRates":
              return 4;
          default:
              return -1;
      }  
    }
}
