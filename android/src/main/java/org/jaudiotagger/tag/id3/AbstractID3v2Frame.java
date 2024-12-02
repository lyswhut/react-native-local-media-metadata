/*
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.jaudiotagger.tag.id3;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.id3.framebody.*;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;
import org.jaudiotagger.utils.EqualsUtil;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;

/**
 * This abstract class is each frame header inside a ID3v2 tag.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id$
 */
public abstract class AbstractID3v2Frame extends AbstractTagFrame implements TagTextField
{

    protected static final String TYPE_FRAME = "frame";
    protected static final String TYPE_FRAME_SIZE = "frameSize";
    protected static final String UNSUPPORTED_ID = "Unsupported";

    //Frame identifier
    protected String identifier = "";

    //Frame Size
    protected int frameSize;

    //The purpose of this is to provide the filename that should be used when writing debug messages
    //when problems occur reading or writing to file, otherwise it is difficult to track down the error
    //when processing many files
    private String loggingFilename = "";

    /**
     *
     * @return size in bytes of the frameid field
     */
    protected abstract int getFrameIdSize();

    /**
     *
     * @return the size in bytes of the frame size field
     */
    protected abstract int getFrameSizeSize();

    /**
     *
     * @return the size in bytes of the frame header
     */
    protected abstract int getFrameHeaderSize();

    /**
     * Create an empty frame
     */
    protected AbstractID3v2Frame()
    {
        }

    /**
     * This holds the Status flags (not supported in v2.20
     */
    StatusFlags statusFlags = null;

    /**
     * This holds the Encoding flags (not supported in v2.20)
     */
    EncodingFlags encodingFlags = null;

    /**
     * Create a frame based on another frame
     * @param frame
     */
    public AbstractID3v2Frame(AbstractID3v2Frame frame)
    {
        super(frame);
    }

    /**
     * Create a frame based on a body
     * @param body
     */
    public AbstractID3v2Frame(AbstractID3v2FrameBody body)
    {
        this.frameBody = body;
        this.frameBody.setHeader(this);
    }

    /**
     * Create a new frame with empty body based on identifier
     * @param identifier
     */
    //TODO the identifier checks should be done in the relevent subclasses
    public AbstractID3v2Frame(String identifier)
    {
        logger.config("Creating empty frame of type" + identifier);
        this.identifier = identifier;

        // Use reflection to map id to frame body, which makes things much easier
        // to keep things up to date.
        try
        {
            @SuppressWarnings("unchecked")
			Class<AbstractID3v2FrameBody> c = (Class<AbstractID3v2FrameBody>) Class.forName("org.jaudiotagger.tag.id3.framebody.FrameBody" + identifier);
            frameBody = c.newInstance();
        }
        catch (ClassNotFoundException cnfe)
        {
            logger.severe(cnfe.getMessage());
            frameBody = new FrameBodyUnsupported(identifier);
        }
        //Instantiate Interface/Abstract should not happen
        catch (InstantiationException ie)
        {
            logger.log(Level.SEVERE, "InstantiationException:" + identifier, ie);
            throw new RuntimeException(ie);
        }
        //Private Constructor shouild not happen
        catch (IllegalAccessException iae)
        {
            logger.log(Level.SEVERE, "IllegalAccessException:" + identifier, iae);
            throw new RuntimeException(iae);
        }
        frameBody.setHeader(this);
        if (this instanceof ID3v24Frame)
        {
            frameBody.setTextEncoding(TagOptionSingleton.getInstance().getId3v24DefaultTextEncoding());
        }
        else if (this instanceof ID3v23Frame)
        {
            frameBody.setTextEncoding(TagOptionSingleton.getInstance().getId3v23DefaultTextEncoding());
        }

        logger.config("Created empty frame of type" + identifier);
    }

    /**
     * Retrieve the logging filename to be used in debugging
     *
     * @return logging filename to be used in debugging
     */
    protected String getLoggingFilename()
    {
        return loggingFilename;
    }

    /**
     * Set logging filename when construct tag for read from file
     *
     * @param loggingFilename
     */
    protected void setLoggingFilename(String loggingFilename)
    {
        this.loggingFilename = loggingFilename;
    }

    /**
     * Return the frame identifier, this only identifies the frame it does not provide a unique
     * key, when using frames such as TXXX which are used by many fields     *
     *
     * @return the frame identifier (Tag Field Interface)
     */
    //TODO, this is confusing only returns the frameId, which does not neccessarily uniquely
    //identify the frame
    public String getId()
    {
        return getIdentifier();
    }

    /**
     * Return the frame identifier
     *
     * @return the frame identifier
     */
    public String getIdentifier()
    {
        return identifier;
    }

    //TODO:needs implementing but not sure if this method is required at all
    public void copyContent(TagField field)
    {

    }

    /**
     * Read the frameBody when frame marked as encrypted
     *
     * @param identifier
     * @param byteBuffer
     * @param frameSize
     * @return
     * @throws InvalidFrameException
     * @throws InvalidDataTypeException
     * @throws InvalidTagException
     */
    protected AbstractID3v2FrameBody readEncryptedBody(String identifier, ByteBuffer byteBuffer, int frameSize)
            throws InvalidFrameException, InvalidDataTypeException 
    {
        try
        {
            AbstractID3v2FrameBody frameBody = new  FrameBodyEncrypted(identifier,byteBuffer, frameSize);
            frameBody.setHeader(this);
            return frameBody;
        }
        catch(InvalidTagException ite)
        {
            throw new InvalidDataTypeException(ite);
        }
    }

    protected boolean isPadding(byte[] buffer)
    {
        if(
                (buffer[0]=='\0')&&
                (buffer[1]=='\0')&&
                (buffer[2]=='\0')&&
                (buffer[3]=='\0')
           )
        {
            return true;
        }
        return false;
    }

    /**
     * Read the frame body from the specified file via the buffer
     *
     * @param identifier the frame identifier
     * @param byteBuffer to read the frame body from
     * @param frameSize
     * @return a newly created FrameBody
     * @throws InvalidFrameException unable to construct a framebody from the data
     */
    protected AbstractID3v2FrameBody readBody(String identifier, ByteBuffer byteBuffer, int frameSize)
            throws InvalidFrameException, InvalidDataTypeException
    {
        //Map to FrameBody (keep old reflection code in case we miss any)
        //In future maybe able to do https://stackoverflow.com/questions/61662231/better-alternative-to-reflection-than-large-switch-statement-using-java-8
        logger.finest("Creating framebody:start");
        AbstractID3v2FrameBody frameBody;
        try
        {
            switch (identifier)
            {
                case ID3v24Frames.FRAME_ID_AUDIO_ENCRYPTION:
                    frameBody = new FrameBodyAENC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ATTACHED_PICTURE:
                    frameBody = new FrameBodyAPIC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_AUDIO_SEEK_POINT_INDEX:
                    frameBody = new FrameBodyASPI(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_CHAPTER:
                    frameBody = new FrameBodyCHAP(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_COMMENT:
                    frameBody = new FrameBodyCOMM(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_COMMERCIAL_FRAME:
                    frameBody = new FrameBodyCOMR(byteBuffer, frameSize);
                    break;

                case ID3v22Frames.FRAME_ID_V2_ENCRYPTED_FRAME:
                    frameBody = new FrameBodyCRM(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_CHAPTER_TOC:
                    frameBody = new FrameBodyCTOC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ENCRYPTION:
                    frameBody = new FrameBodyENCR(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_EQUALISATION2:
                    frameBody = new FrameBodyEQU2(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_EVENT_TIMING_CODES:
                    frameBody = new FrameBodyETCO(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_GENERAL_ENCAPS_OBJECT:
                    frameBody = new FrameBodyGEOB(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ITUNES_GROUPING:
                    frameBody = new FrameBodyGRP1(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_GROUP_ID_REG:
                    frameBody = new FrameBodyGRID(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_INVOLVED_PEOPLE:
                    frameBody = new FrameBodyIPLS(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_LINKED_INFO:
                    frameBody = new FrameBodyLINK(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_MUSIC_CD_ID:
                    frameBody = new FrameBodyMCDI(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_MOVEMENT_NO:
                    frameBody = new FrameBodyMVIN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_MOVEMENT:
                    frameBody = new FrameBodyMVNM(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_OWNERSHIP:
                    frameBody = new FrameBodyOWNE(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_PLAY_COUNTER:
                    frameBody = new FrameBodyPCNT(byteBuffer, frameSize);
                    break;

                case ID3v22Frames.FRAME_ID_V2_ATTACHED_PICTURE:
                    frameBody = new FrameBodyPIC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_POPULARIMETER:
                    frameBody = new FrameBodyPOPM(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_POSITION_SYNC:
                    frameBody = new FrameBodyPOSS(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_PRIVATE:
                    frameBody = new FrameBodyPRIV(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_RECOMMENDED_BUFFER_SIZE:
                    frameBody = new FrameBodyRBUF(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_RELATIVE_VOLUME_ADJUSTMENT2:
                    frameBody = new FrameBodyRVA2(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT:
                    frameBody = new FrameBodyRVAD(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_REVERB:
                    frameBody = new FrameBodyRVRB(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_SEEK:
                    frameBody = new FrameBodySEEK(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_SIGNATURE:
                    frameBody = new FrameBodySIGN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_SYNC_LYRIC:
                    frameBody = new FrameBodySYLT(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_SYNC_TEMPO:
                    frameBody = new FrameBodySYTC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ALBUM:
                    frameBody = new FrameBodyTALB(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_BPM:
                    frameBody = new FrameBodyTBPM(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_IS_COMPILATION:
                    frameBody = new FrameBodyTCMP(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_GENRE:
                    frameBody = new FrameBodyTCON(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_COPYRIGHTINFO:
                    frameBody = new FrameBodyTCOP(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_TDAT:
                    frameBody = new FrameBodyTDAT(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ENCODING_TIME:
                    frameBody = new FrameBodyTDEN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_PLAYLIST_DELAY:
                    frameBody = new FrameBodyTDLY(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ORIGINAL_RELEASE_TIME:
                    frameBody = new FrameBodyTDOR(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_YEAR:
                    frameBody = new FrameBodyTDRC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_RELEASE_TIME:
                    frameBody = new FrameBodyTDRL(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_TAGGING_TIME:
                    frameBody = new FrameBodyTDTG(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ENCODEDBY:
                    frameBody = new FrameBodyTENC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_LYRICIST:
                    frameBody = new FrameBodyTEXT(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_FILE_TYPE:
                    frameBody = new FrameBodyTFLT(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_TIME:
                    frameBody = new FrameBodyTIME(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE:
                    frameBody = new FrameBodyTIPL(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_CONTENT_GROUP_DESC:
                    frameBody = new FrameBodyTIT1(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_TITLE:
                    frameBody = new FrameBodyTIT2(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_TITLE_REFINEMENT:
                    frameBody = new FrameBodyTIT3(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_INITIAL_KEY:
                    frameBody = new FrameBodyTKEY(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_LANGUAGE:
                    frameBody = new FrameBodyTLAN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_LENGTH:
                    frameBody = new FrameBodyTLEN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_MUSICIAN_CREDITS:
                    frameBody = new FrameBodyTMCL(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_MEDIA_TYPE:
                    frameBody = new FrameBodyTMED(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_MOOD:
                    frameBody = new FrameBodyTMOO(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ORIG_TITLE:
                    frameBody = new FrameBodyTOAL(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ORIG_FILENAME:
                    frameBody = new FrameBodyTOFN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ORIG_LYRICIST:
                    frameBody = new FrameBodyTOLY(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ORIGARTIST:
                    frameBody = new FrameBodyTOPE(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_TORY:
                    frameBody = new FrameBodyTORY(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_FILE_OWNER:
                    frameBody = new FrameBodyTOWN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ARTIST:
                    frameBody = new FrameBodyTPE1(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ACCOMPANIMENT:
                    frameBody = new FrameBodyTPE2(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_CONDUCTOR:
                    frameBody = new FrameBodyTPE3(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_REMIXED:
                    frameBody = new FrameBodyTPE4(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_SET:
                    frameBody = new FrameBodyTPOS(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_PRODUCED_NOTICE:
                    frameBody = new FrameBodyTPRO(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_PUBLISHER:
                    frameBody = new FrameBodyTPUB(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_TRACK:
                    frameBody = new FrameBodyTRCK(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_TRDA:
                    frameBody = new FrameBodyTRDA(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_RADIO_NAME:
                    frameBody = new FrameBodyTRSN(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_RADIO_OWNER:
                    frameBody = new FrameBodyTRSO(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_TSIZ:
                    frameBody = new FrameBodyTSIZ(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ALBUM_ARTIST_SORT_ORDER_ITUNES:
                    frameBody = new FrameBodyTSO2(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ALBUM_SORT_ORDER:
                    frameBody = new FrameBodyTSOA(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_COMPOSER_SORT_ORDER_ITUNES:
                    frameBody = new FrameBodyTSOC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ARTIST_SORT_ORDER:
                    frameBody = new FrameBodyTSOP(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_TITLE_SORT_ORDER:
                    frameBody = new FrameBodyTSOT(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_ISRC:
                    frameBody = new FrameBodyTSRC(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_HW_SW_SETTINGS:
                    frameBody = new FrameBodyTSSE(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_SET_SUBTITLE:
                    frameBody = new FrameBodyTSST(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_USER_DEFINED_INFO:
                    frameBody = new FrameBodyTXXX(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_TYER:
                    frameBody = new FrameBodyTYER(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_UNIQUE_FILE_ID:
                    frameBody = new FrameBodyUFID(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_TERMS_OF_USE:
                    frameBody = new FrameBodyUSER(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_UNSYNC_LYRICS:
                    frameBody = new FrameBodyUSLT(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_COMMERCIAL:
                    frameBody = new FrameBodyWCOM(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_COPYRIGHT:
                    frameBody = new FrameBodyWCOP(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_FILE_WEB:
                    frameBody = new FrameBodyWOAF(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_ARTIST_WEB:
                    frameBody = new FrameBodyWOAR(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_SOURCE_WEB:
                    frameBody = new FrameBodyWOAS(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_OFFICIAL_RADIO:
                    frameBody = new FrameBodyWORS(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_PAYMENT:
                    frameBody = new FrameBodyWPAY(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_URL_PUBLISHERS:
                    frameBody = new FrameBodyWPUB(byteBuffer, frameSize);
                    break;

                case ID3v24Frames.FRAME_ID_USER_DEFINED_URL:
                    frameBody = new FrameBodyWXXX(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ:
                    frameBody = new FrameBodyXSOA(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ:
                    frameBody = new FrameBodyXSOP(byteBuffer, frameSize);
                    break;

                case ID3v23Frames.FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ:
                    frameBody = new FrameBodyXSOT(byteBuffer, frameSize);
                    break;

                //Catch-all incase we have missed any
                default:
                    @SuppressWarnings("unchecked") Class<AbstractID3v2FrameBody> c = (Class<AbstractID3v2FrameBody>) Class.forName("org.jaudiotagger.tag.id3.framebody.FrameBody" + identifier);
                    Class<?>[] constructorParameterTypes = {Class.forName("java.nio.ByteBuffer"), Integer.TYPE};
                    Object[] constructorParameterValues = {byteBuffer, frameSize};
                    Constructor<AbstractID3v2FrameBody> construct = c.getConstructor(constructorParameterTypes);
                    frameBody = (construct.newInstance(constructorParameterValues));
            }
        }
        catch(InvalidTagException e)
        {
            throw new InvalidFrameException(e.getMessage());
        }
        //No class defined for this frame type,use FrameUnsupported
        catch (ClassNotFoundException cex)
        {
            logger.config(getLoggingFilename() + ":" + "Identifier not recognised:" + identifier + " using FrameBodyUnsupported");
            try
            {
                frameBody = new FrameBodyUnsupported(byteBuffer, frameSize);
            }
            //Should only throw InvalidFrameException but unfortunately legacy hierachy forces
            //read method to declare it can throw InvalidtagException
            catch (InvalidFrameException ife)
            {
                throw ife;
            }
            catch (InvalidTagException te)
            {
                throw new InvalidFrameException(te.getMessage());
            }
        }
        //An error has occurred during frame instantiation, if underlying cause is an unchecked exception or error
        //propagate it up otherwise mark this frame as invalid
        catch (InvocationTargetException ite)
        {
            logger.severe(getLoggingFilename() + ":" + "An error occurred within abstractID3v2FrameBody for identifier:" + identifier + ":" + ite.getCause().getMessage());
            if (ite.getCause() instanceof Error)
            {
                throw (Error) ite.getCause();
            }
            else if (ite.getCause() instanceof RuntimeException)
            {
                throw (RuntimeException) ite.getCause();
            }
            else if (ite.getCause() instanceof InvalidFrameException)
            {
                throw (InvalidFrameException) ite.getCause();
            }
            else if (ite.getCause() instanceof InvalidDataTypeException)
            {
                throw (InvalidDataTypeException) ite.getCause();
            }
            else
            {
                throw new InvalidFrameException(ite.getCause().getMessage());
            }
        }
        //No Such Method should not happen
        catch (NoSuchMethodException sme)
        {
            logger.log(Level.SEVERE, getLoggingFilename() + ":" + "No such method:" + sme.getMessage(), sme);
            throw new RuntimeException(sme.getMessage());
        }
        //Instantiate Interface/Abstract should not happen
        catch (InstantiationException ie)
        {
            logger.log(Level.SEVERE, getLoggingFilename() + ":" + "Instantiation exception:" + ie.getMessage(), ie);
            throw new RuntimeException(ie.getMessage());
        }
        //Private Constructor shouild not happen
        catch (IllegalAccessException iae)
        {
            logger.log(Level.SEVERE, getLoggingFilename() + ":" + "Illegal access exception :" + iae.getMessage(), iae);
            throw new RuntimeException(iae.getMessage());
        }

        logger.finest(getLoggingFilename() + ":" + "Created framebody:end" + frameBody.getIdentifier());
        frameBody.setHeader(this);
        return frameBody;

    }

    /**
     * Get the next frame id, throwing an exception if unable to do this and check against just having padded data
     * 
     * @param byteBuffer
     * @return
     * @throws PaddingException
     * @throws InvalidFrameException
     */
    protected String readIdentifier(ByteBuffer byteBuffer) throws PaddingException,InvalidFrameException
    {
        byte[] buffer = new byte[getFrameIdSize()];

        //Read the Frame Identifier
        if(getFrameIdSize()<=byteBuffer.remaining())
        {
            byteBuffer.get(buffer, 0, getFrameIdSize());
        }

        if(isPadding(buffer))
        {
            throw new PaddingException(getLoggingFilename() + ":only padding found");
        }

        if ((getFrameHeaderSize() - getFrameIdSize()) > byteBuffer.remaining())
        {
            logger.warning(getLoggingFilename() + ":" + "No space to find another frame:");
            throw new InvalidFrameException(getLoggingFilename() + ":" + "No space to find another frame");
        }


        identifier = new String(buffer);
        logger.fine(getLoggingFilename() + ":" + "Identifier is" + identifier);
        return identifier;
    }

    /**
     * This creates a new body based of type identifier but populated by the data
     * in the body. This is a different type to the body being created which is why
     * TagUtility.copyObject() can't be used. This is used when converting between
     * different versions of a tag for frames that have a non-trivial mapping such
     * as TYER in v3 to TDRC in v4. This will only work where appropriate constructors
     * exist in the frame body to be created, for example a FrameBodyTYER requires a constructor
     * consisting of a FrameBodyTDRC.
     *
     * If this method is called and a suitable constructor does not exist then an InvalidFrameException
     * will be thrown
     *
     * @param identifier to determine type of the frame
     * @param body
     * @return newly created framebody for this type
     * @throws InvalidFrameException if unable to construct a framebody for the identifier and body provided.
     */
    @SuppressWarnings("unchecked")
    //TODO using reflection is rather slow perhaps we should change this
    protected AbstractID3v2FrameBody readBody(String identifier, AbstractID3v2FrameBody body) throws InvalidFrameException
    {
        /* Use reflection to map id to frame body, which makes things much easier
         * to keep things up to date, although slight performance hit.
         */
        AbstractID3v2FrameBody frameBody;
        try
        {
            Class<AbstractID3v2FrameBody> c = (Class<AbstractID3v2FrameBody>) Class.forName("org.jaudiotagger.tag.id3.framebody.FrameBody" + identifier);
            Class<?>[] constructorParameterTypes = {body.getClass()};
            Object[] constructorParameterValues = {body};
            Constructor<AbstractID3v2FrameBody> construct = c.getConstructor(constructorParameterTypes);
            frameBody = (construct.newInstance(constructorParameterValues));
        }
        catch (ClassNotFoundException cex)
        {
            logger.config("Identifier not recognised:" + identifier + " unable to create framebody");
            throw new InvalidFrameException("FrameBody" + identifier + " does not exist");
        }
        //If suitable constructor does not exist
        catch (NoSuchMethodException sme)
        {
            logger.log(Level.SEVERE, "No such method:" + sme.getMessage(), sme);
            throw new InvalidFrameException("FrameBody" + identifier + " does not have a constructor that takes:" + body.getClass().getName());
        }
        catch (InvocationTargetException ite)
        {
            logger.severe("An error occurred within abstractID3v2FrameBody");
            logger.log(Level.SEVERE, "Invocation target exception:" + ite.getCause().getMessage(), ite.getCause());
            if (ite.getCause() instanceof Error)
            {
                throw (Error) ite.getCause();
            }
            else if (ite.getCause() instanceof RuntimeException)
            {
                throw (RuntimeException) ite.getCause();
            }
            else
            {
                throw new InvalidFrameException(ite.getCause().getMessage());
            }
        }

        //Instantiate Interface/Abstract should not happen
        catch (InstantiationException ie)
        {
            logger.log(Level.SEVERE, "Instantiation exception:" + ie.getMessage(), ie);
            throw new RuntimeException(ie.getMessage());
        }
        //Private Constructor shouild not happen
        catch (IllegalAccessException iae)
        {
            logger.log(Level.SEVERE, "Illegal access exception :" + iae.getMessage(), iae);
            throw new RuntimeException(iae.getMessage());
        }

        logger.finer("frame Body created" + frameBody.getIdentifier());
        frameBody.setHeader(this);
        return frameBody;
    }

    public byte[] getRawContent()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos);
        return baos.toByteArray();
    }

    public abstract void write(ByteArrayOutputStream tagBuffer);

    /**
     * @param b
     */
    public void isBinary(boolean b)
    {
        //do nothing because whether or not a field is binary is defined by its id and is immutable
    }


    public boolean isEmpty()
    {
        AbstractTagFrameBody body = this.getBody();
        if (body == null)
        {
            return true;
        }
        //TODO depends on the body
        return false;
    }

    public StatusFlags getStatusFlags()
    {
        return statusFlags;
    }

    public EncodingFlags getEncodingFlags()
    {
        return encodingFlags;
    }

    public class StatusFlags
    {
        protected static final String TYPE_FLAGS = "statusFlags";

        protected byte originalFlags;
        protected byte writeFlags;

        protected StatusFlags()
        {

        }

        /**
         * This returns the flags as they were originally read or created
         * @return
         */
        public byte getOriginalFlags()
        {
            return originalFlags;
        }

        /**
         * This returns the flags amended to meet specification
         * @return
         */
        public byte getWriteFlags()
        {
            return writeFlags;
        }

        public void createStructure()
        {
        }

        public boolean equals(Object obj)
        {
            if ( this == obj ) return true;

            if (!(obj instanceof StatusFlags))
            {
                return false;
            }
            StatusFlags that = (StatusFlags) obj;


            return
                  EqualsUtil.areEqual(this.getOriginalFlags(), that.getOriginalFlags()) &&
                  EqualsUtil.areEqual(this.getWriteFlags(), that.getWriteFlags()) ;

        }
    }

    class EncodingFlags
    {
        protected static final String TYPE_FLAGS = "encodingFlags";

        protected byte flags;

        protected EncodingFlags()
        {
            resetFlags();
        }

        protected EncodingFlags(byte flags)
        {
            setFlags(flags);
        }

        public byte getFlags()
        {
            return flags;
        }

        public void setFlags(byte flags)
        {
            this.flags = flags;
        }

        public void resetFlags()
        {
            setFlags((byte) 0);
        }

        public void createStructure()
        {
        }

        public boolean equals(Object obj)
        {
            if ( this == obj ) return true;

            if (!(obj instanceof EncodingFlags))
            {
                return false;
            }
            EncodingFlags that = (EncodingFlags) obj;


            return EqualsUtil.areEqual(this.getFlags(), that.getFlags());

        }
    }

    /**
     * Return String Representation of frame
     */
    public void createStructure()
    {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_FRAME, getIdentifier());
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_FRAME);
    }

    public boolean equals(Object obj)
    {
        if ( this == obj ) return true;
        if (!(obj instanceof AbstractID3v2Frame))
        {
            return false;
        }

        AbstractID3v2Frame that = (AbstractID3v2Frame) obj;
        return super.equals(that);
    }

    /**
     * Returns the content of the field.
     *
     * For frames consisting of different fields, this will return the value deemed to be most
     * likely to be required
     *
     * @return Content
     */
    public String getContent()
    {
        return getBody().getUserFriendlyValue();
    }

    /**
     * Returns the current used charset encoding.
     *
     * @return Charset encoding.
     */
    public Charset getEncoding()
    {
        final byte textEncoding = this.getBody().getTextEncoding();
        return TextEncoding.getInstanceOf().getCharsetForId(textEncoding);
    }

    /**
     * Sets the content of the field.
     *
     * @param content fields content.
     */
    public void setContent(String content)
    {
        throw new UnsupportedOperationException("Not implemented please use the generic tag methods for setting content");
    }

}
