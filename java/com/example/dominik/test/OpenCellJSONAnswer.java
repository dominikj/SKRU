package com.example.dominik.test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by dominik on 2016-04-11.
 */

public class OpenCellJSONAnswer {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String lat;
    private String lon;
    private String radio;
    private String mcc;
    private String mnc;
    private String lac;
    private String cellid;
    private String averageSignalStrength;
    private String range;
    private String samples;
    private String changeable;
    private String rnc;
    private String cid;
    private String psc;

    public String getLat(){
        return lat;
    }

    public String getLon(){
        return lon;
    }

    public String getRadio(){
        return radio;
    }
    public String getMcc(){
        return mcc;
    }
    public String getMnc(){
        return mnc;
    }
    public String getLac(){
        return lac;
    }
    public String getCellid(){
        return cellid;
    }
    public String getAverageSignalStrength(){
        return averageSignalStrength;
    }
    public String getRange(){
        return range;
    }
    public String getSamples(){
        return samples;
    }
    public String getChangeable(){
        return changeable;
    }
    public String getRnc(){
        return rnc;
    }
    public String getCid(){
        return cid;
    }
    public String getPsc(){
        return psc;
    }


}


  /*  cell lat="52.2247565" lon="21.0151715" mcc="260" mnc="1" lac="11001" cellid="75432121"
        averageSignalStrength="-10" range="0" samples="1" changeable="1" radio="UMTS" rnc="1151" cid="185"/>*/