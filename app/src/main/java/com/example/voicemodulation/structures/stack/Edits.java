package com.example.voicemodulation.structures.stack;
import com.example.voicemodulation.structures.Structure;
import com.example.voicemodulation.structures.sequence.Piece;
import com.example.voicemodulation.structures.sequence.PieceTable;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Edits implements Serializable {
    private final ArrayList<Edit> editStack = new ArrayList<>();
    private ArrayList<Edit> redoStack = new ArrayList<>();
    public Edit currentEdit;
    public Edit currentRedo;
    public int editIndex = -1;
    public int redoIndex = -1;

    public void pushEdit(Edit newEdit) {
        editStack.add(newEdit);
        editIndex += 1;
    }
    private void push(Edit edit){
        editStack.add(edit);
        editIndex+=1;
    }
    private void pop(){
        editStack.remove(editIndex);
        editIndex-=1;
    }
    private void handleRemove(Edit edit,RandomAccessFile buffer){
        byte[] removeSequence = new byte[edit.length];
        try {
            buffer.seek(buffer.length()-edit.length);
            buffer.read(removeSequence);
            buffer.seek(buffer.length());
            buffer.write(removeSequence);
        } catch (IOException ex) {
            Logger.getLogger(Edits.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PieceTable undo(PieceTable sequence, RandomAccessFile buffer){
        if(editIndex>=0){
            currentEdit = editStack.get(editIndex);
            System.out.println("edit length = "+ currentEdit.length+ " offset = "+ currentEdit.offset+" type = "+ currentEdit.editType);
            switch(currentEdit.editType)
            {
                case "addition":
                    sequence.position-=currentEdit.length;
                    sequence.remove(currentEdit.offset,currentEdit.length);
                    break;
                case "remove":
                    handleRemove(currentEdit,buffer);
                    sequence.position=sequence.byte_length+currentEdit.length-1;
                    sequence.add(currentEdit.length,currentEdit.offset);
                    sequence.position-=currentEdit.length;
                    break;
            }
            redoStack.add(currentEdit);
            redoIndex+=1;
            pop();
        }
        return sequence;
    }
    public PieceTable redo(PieceTable sequence, RandomAccessFile buffer){
        System.out.println("redoIndex="+redoIndex);
        printRedo();
        if(redoIndex>=0){
            currentRedo = redoStack.get(redoIndex);
            System.out.println("edit length = "+ currentRedo.length+ " offset = "+ currentRedo.offset+" type = "+ currentRedo.editType);
            switch(currentRedo.editType){
                case "addition":
                    System.out.println("redoing an addition");
                    sequence.add(currentRedo.length,currentRedo.offset);
                    break;
                case "remove":
                    sequence.position-=currentEdit.length;
                    sequence.remove(currentRedo.offset, currentRedo.length);
                    break;
            }
            push(currentRedo);
            redoStack.remove(redoIndex);
            redoIndex-=1;
        }
        return sequence;
    }


    public void printEdits(){
        System.out.println("Edit Stack");
        for(Edit edit: editStack){
            System.out.println("editType: "+edit.editType+", editOffset: "+edit.offset+", editLength: "+edit.length);
        }
    }
    public void printRedo(){
        System.out.println("Redo Stack");
        for(Edit edit: redoStack){
            System.out.println("editType: "+edit.editType+", editOffset: "+edit.offset+", editLength: "+edit.length);
        }
    }
}
