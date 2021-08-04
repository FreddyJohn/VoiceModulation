package com.example.voicemodulation.edits;

import com.example.voicemodulation.sequence.Piece;
import com.example.voicemodulation.sequence.PieceTable;

import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Edits {
    private final ArrayList<Edit> edits = new ArrayList<>();
    private ArrayList<Edit> redos = new ArrayList<>();
    private Edit currentEdit;
    private Edit currentRedo;
    public int editIndex;
    public int redoIndex;
    private void push(Edit edit){
        edits.add(edit);
        editIndex+=1;
    }
    private void pop(){
        edits.remove(editIndex-1);
        editIndex-=1;
    }
    public void pushEdit(Edit newEdit){
        edits.add(newEdit);
        redos = new ArrayList<>();
        redoIndex= -1;
        editIndex+=1;
    }
    public PieceTable undo(PieceTable sequence, RandomAccessFile buffer){
        if(editIndex>0){
            currentEdit = edits.get(editIndex-1);
            switch(currentEdit.editType)
            {
                case "addition":
                    sequence.remove(currentEdit.offset,currentEdit.length);
                    break;
                case "remove":
                    sequence.add(currentEdit.offset,currentEdit.length);
            }
            redos.add(currentEdit);
            redoIndex+=1;
            pop();
        }
        return sequence;
    }
    public PieceTable redo(PieceTable sequence, RandomAccessFile buffer){
        if(redoIndex>=0){
            currentRedo = redos.get(redoIndex);
            switch(currentRedo.editType){
                case "addition":
                    sequence.add(currentRedo.length,currentRedo.offset);
                    break;
                case "remove":
                    sequence.remove(currentRedo.offset, currentRedo.length);
            }
            push(currentRedo);
            redos.remove(redoIndex);
            redoIndex-=1;
        }
        return sequence;
    }
}
