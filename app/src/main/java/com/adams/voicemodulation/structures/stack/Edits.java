package com.adams.voicemodulation.structures.stack;
import com.adams.voicemodulation.structures.sequence.PieceTable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Edits implements Serializable {
    private ArrayList<Edit> editStack = new ArrayList<>();
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
    private void handleRemove(Edit edit,RandomAccessFile buffer,RandomAccessFile removeStack){
        byte[] removeSequence = new byte[edit.length];
        try {
            removeStack.seek(0);
            removeStack.read(removeSequence);
            buffer.seek(buffer.length());
            buffer.write(removeSequence);
        } catch (IOException ex) {
            Logger.getLogger(Edits.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PieceTable undo(PieceTable sequence, RandomAccessFile buffer,RandomAccessFile originalBuffer,RandomAccessFile removeStack){
        if(editIndex>=0){
            currentEdit = editStack.get(editIndex);
            switch(currentEdit.editType)
            {
                case "addition":
                    currentEdit.offset = (currentEdit.offset==0) ? sequence.unit : currentEdit.offset;
                    currentEdit.length = (currentEdit.length==sequence.byte_length) ? currentEdit.length-sequence.unit: currentEdit.length;
                    sequence.remove(currentEdit.offset,currentEdit.length);
                    break;
                case "remove":
                    handleRemove(currentEdit,buffer,removeStack);
                    sequence.add(currentEdit.length,currentEdit.offset,buffer);
                    break;

            }
            redoStack.add(currentEdit);
            redoIndex+=1;
            pop();
        }else{
            System.out.println("At initial state");
        }
        return sequence;
    }

    public PieceTable redo(PieceTable sequence, RandomAccessFile buffer, RandomAccessFile originalBuffer){
        printRedo();
        if(redoIndex>=0){
            currentRedo = redoStack.get(redoIndex);
            switch(currentRedo.editType){
                case "addition":
                    if (currentRedo.in_added){
                        try {
                            byte[] bytes = new byte[(int) originalBuffer.length()];
                            originalBuffer.seek(0);
                            originalBuffer.read(bytes);
                            buffer.seek(buffer.length());
                            buffer.write(bytes);
                        }catch(IOException ignored){ }
                    }
                    sequence.add(currentRedo.length,currentRedo.offset,buffer);
                    break;
                case "remove":
                    sequence.remove(currentRedo.offset, currentRedo.length);
                    break;

            }
            push(currentRedo);
            redoStack.remove(redoIndex);
            redoIndex-=1;
        }else{
            System.out.println("At latest state");
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
    public void emptyRedoStack(){
        redoStack=new ArrayList<>();
        redoIndex = -1;
    }

    public void emptyUndoStack() {
        editStack = new ArrayList<>();
        editIndex = -1;
    }
    public void emptyEdits() {
        emptyUndoStack();
        emptyRedoStack();
    }
}
