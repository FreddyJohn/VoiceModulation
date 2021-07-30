package com.example.voicemodulation.database.project;

import com.example.voicemodulation.sequence.Piece;

import java.util.ArrayList;

/*
TODO remove use of serializable for PieceTableLogic and store the sequence as an embedded object within its
    respective project table.
    then later in PieceTable api we will replace serialize and deserialize methods with db writes/reads
    this way the data is separate from the logic that utilizes the data.
 */
public class Sequences {
    public ArrayList<Piece> audio_sequence;
    
}
