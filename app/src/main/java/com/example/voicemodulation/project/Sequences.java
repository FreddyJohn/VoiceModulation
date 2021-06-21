package com.example.voicemodulation.project;

import com.example.voicemodulation.sequence.PieceTableLogic;

import java.io.Serializable;

public class Sequences implements Serializable {
    public PieceTableLogic audioPieceTable;
    public PieceTableLogic bitmapPieceTable;
    public Paths projectPaths;
    public Sequences(Paths projectPaths){
        this.projectPaths = projectPaths;
    }
    public void setTables(PieceTableLogic audioPieceTable,
                          PieceTableLogic bitmapPieceTable){
        this.audioPieceTable = audioPieceTable;
        this.bitmapPieceTable = bitmapPieceTable;
    }
}
