package com.example.voicemodulation.signal;

import com.example.voicemodulation.graph.GraphLogic;
import com.example.voicemodulation.structures.Structure;

import java.io.RandomAccessFile;

public class Mixer {
    /*
        TODO ->  Implement mixer
            1.) select two different spans using graphLogic
            2.) on some event within GraphLogic pass the BytePoints,RandomAccessFile,
             and Structure for the given audio project to static Mix method
            3.) within Mix retrieve the bytes for the selected spans
            4.) convert the bytes into their appropriate data type
            5.) preform the mixing algorithm to combine two spans into one
            6.) delete the unmixed span currently in the spot our mixed one should be
             (write the deleted span to the remove stack buffer to support undo)
            7.) write the mixed span to the end of audio file
            8.) finally, add the span to the Structure at the insert location given by BytePoints and return
     */

    /*
        Implementation notes
            One big problem is going to be that we are not changing the waveform for modulations
            or for mixing. for modulations I would like to just apply a sequence of color mask overtop
            the modulated spans. For mixing I think I would like to actually do something to the waveform
            to show these two signals are now one. consider mixing spans with peaks opposite to each other.
            without doing anything to the waveform you would be confused as to what had happened. The alternative,
            is to have a multi-track UI this however will require a lot of redesign and added logic to deal with
            the scroll about the y axis in GraphLogic to view and select from n tracks. Really the problem is, how would
            you even make selections with a touch screen multi-track UI in an elegant way?

     */

    public static void Mix(Structure structure, GraphLogic.BytePoints points, RandomAccessFile file){


    }
}

/*
    PYTHON IMPLEMENTATION:

            from struct import unpack, pack
            import numpy as np

            def get_bytes(file_path):
                bytes=b''
                with open(file_path,'rb') as f:
                    bytes=f.read()
                return bytes

            def get_shorts(bytes):
                count = len(bytes)//2
                return unpack('h'*count,bytes)

            def mix(s1,s2):
                s1,s2=fit(s1,s2)
                mixed=[]
                for i in range(len(s1)):
                    s=(s1[i]+s2[i])
                    mixed.append(int(s)//2)
                return pack('h'*len(s1),*mixed)

            def fit(s1,s2):
                if len(s1)>len(s2):
                    s1=s1[:len(s2)]
                elif len(s1)<len(s2):
                    s2=s2[:len(s1)]
                return s1,s2

            def writeWav(pcm):

                sampleRate=48000
                num_channels=1
                samples=len(pcm)
                subchunk2size=samples*num_channels*(16//8)+36
                byterate=sampleRate*1*(16//8)
                blockalign=1*(16//8)

                with open("sumSignal.wav",'wb') as f:
                    f.write(pack('>I',0x52494646)) #RIFF
                    f.write(pack('<i',samples+36))
                    f.write(pack('>I',0x57415645)) #WAVE
                    f.write(pack('>I',0x666d7420)) #FMT
                    f.write(pack('<L',16))
                    f.write(pack('<H',1))
                    f.write(pack('<H',1))
                    f.write(pack('<L',sampleRate))
                    f.write(pack('<L',byterate))
                    f.write(pack('<H',blockalign))
                    f.write(pack('<H',16))
                    f.write(pack('>I',0x64617461)) #data
                    f.write(pack('<i',samples))
                    f.write(pcm)

                from playsound import playsound
                playsound('sumSignal.wav')

            bytes_1=get_bytes('yourMom.wav')
            bytes_2=get_bytes('rec.wav')

            shorts_1=get_shorts(bytes_1)
            shorts_2=get_shorts(bytes_2)

            #writeWav(bytes_2)
            #writeWav(bytes_1)
            writeWav(mix(shorts_1,shorts_2))



 */