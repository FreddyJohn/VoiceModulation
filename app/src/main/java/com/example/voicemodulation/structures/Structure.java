package com.example.voicemodulation.structures;

import com.example.voicemodulation.structures.sequence.PieceTable;
import com.example.voicemodulation.structures.stack.Edit;
import com.example.voicemodulation.structures.stack.Edits;
import com.example.voicemodulation.util.Persist;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Structure {
    public Persist<PieceTable> pieceTablePersist;
    public Persist<Edits> editsPersist;
    private RandomAccessFile removeStack;
    private RandomAccessFile editsBuffer;
    private RandomAccessFile originalBuffer;
    private PieceTable pieceTable;
    public int byte_length;
    private Edits edits;
    private int unit;

    public Structure(String objectPath, String editPath, String originalPath, String editsPath,String rPath) {
        this.pieceTablePersist = new Persist<>();
        this.editsPersist = new Persist<>();
        pieceTablePersist.setOutputFile(objectPath);
        editsPersist.setOutputFile(editsPath);
        try {

            originalBuffer = new RandomAccessFile(originalPath,"rw");
            removeStack = new RandomAccessFile(rPath,"rw");
            editsBuffer = new RandomAccessFile(editPath, "rw");
            RandomAccessFile editsStack = new RandomAccessFile(editsPath, "rw");

            pieceTable = removeStack.length()!=0 | editsBuffer.length()!=0 | originalBuffer.length() !=0 ?
                    pieceTablePersist.deserialize() : pieceTable;
            byte_length = pieceTable!=null ? pieceTable.byte_length : 0;
            edits = editsStack.length()!=0 ? editsPersist.deserialize() : edits;

        } catch (IOException ex) {
            Logger.getLogger(PieceTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setUnit(int unit){
        pieceTable.setUnit(unit);
        this.unit = unit;
        pieceTablePersist.serialize(pieceTable);
    }

    public void add_original(int length) {
        edits = new Edits();
        pieceTable = new PieceTable();
        edits.pushEdit(new Edit(length,0,"addition",true));
        pieceTable.add_original(0,length);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
    }

    public Structure add(int length, int index) {
        index = (index==0) ? unit : index;
        edits.emptyEdits();
        edits.pushEdit(new Edit(length, index,"addition",false));
        pieceTable.add(length,index,editsBuffer);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
        return this;
    }

    public byte[] getByteSequence() {
        return pieceTable.get_text(editsBuffer, originalBuffer);
    }

    public byte[] find(long index, long length) {
        pieceTable = pieceTablePersist.deserialize();

        if (index==0){
            index=unit;
            length = pieceTable.byte_length - unit;
        }

        return pieceTable.find(index,length, editsBuffer, originalBuffer);
    }

    public void remove(long index, long length) {
        index = (index==0) ? unit : index;
        length = (length==pieceTable.byte_length) ? length-2: length;
        edits.emptyEdits();
        try {
            removeStack.setLength(0);
            removeStack.seek(0);
            removeStack.write(find(index,length));
        } catch (IOException ignored) { }
        edits.pushEdit(new Edit((int)length,(int)index,"remove",false));
        pieceTable = pieceTablePersist.deserialize();
        pieceTable.remove(index,length);
        byte_length = pieceTable.byte_length;
        pieceTablePersist.serialize(pieceTable);
        editsPersist.serialize(edits);
    }

    public void printPieces() {
        pieceTable.print_pieces();
    }

    public Structure getMostRecent(){
        pieceTable = pieceTablePersist.deserialize();
        return this;
    }

    public boolean undo(){
        if (edits!=null && edits.editIndex!=-1) {
            pieceTable = pieceTablePersist.deserialize();
            edits = editsPersist.deserialize();
            pieceTable = edits.undo(pieceTable, editsBuffer, removeStack, originalBuffer);
            byte_length = pieceTable.byte_length;
            pieceTablePersist.serialize(pieceTable);
            editsPersist.serialize(edits);
            return true;
        }
        return false;
    }
    public boolean redo(){
        if(edits!=null && edits.redoIndex!=-1) {
            pieceTable = pieceTablePersist.deserialize();
            edits = editsPersist.deserialize();
            pieceTable = edits.redo(pieceTable, editsBuffer, originalBuffer);
            byte_length = pieceTable.byte_length;
            pieceTablePersist.serialize(pieceTable);
            editsPersist.serialize(edits);
            return true;
        }
        return false;
    }

    public void printEditStack(){
        edits.printEdits();
    }

    public void printRedoStack(){
        edits.printRedo();
    }

    public boolean hasEdits(){
        return pieceTable.pieces.size() <= 1;
    }
}






