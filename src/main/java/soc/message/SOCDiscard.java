/**
 * Java Settlers - An online multiplayer version of the game Settlers of Catan
 * Copyright (C) 2003  Robert S. Thomas <thomas@infolab.northwestern.edu>
 * Portions of this file Copyright (C) 2010,2012,2014,2016-2021 Jeremy D Monin <jeremy@nand.net>
 * Portions of this file Copyright (C) 2017-2018 Strategic Conversation (STAC Project) https://www.irit.fr/STAC/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The maintainer of this program can be reached at jsettlers@nand.net
 **/
package soc.message;

import soc.game.SOCGame;  // for javadocs only
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;

import java.util.StringTokenizer;


/**
 * This message gives the resources that a player has chosen to discard;
 * client's response to server's {@link SOCDiscardRequest}.
 *<P>
 * If the resource total isn't correct, server v2.5.00 and newer will
 * resend {@code SOCDiscardRequest} with the required resource count.
 *<P>
 * If this is the right total amount to discard, server will respond to player
 * with a {@link SOCPlayerElement} or {@link SOCPlayerElements} LOSE message to confirm the details,
 * then report only the discard's resource total to the other players
 * via {@code SOCPlayerElement} and text. See {@link SOCPlayerElement}
 * class javadoc for more details.
 *<P>
 * Server will then send the new {@link SOCGameState}.
 * If waiting for others to discard, server then sends the game a {@link SOCGameServerText} that lists
 * who we're still waiting for. The {@link SOCGameState}({@link SOCGame#WAITING_FOR_DISCARDS WAITING_FOR_DISCARDS})
 * sent is redundant in that case, but server sends it anyway in order to regularize the message sequence
 * to make it easier for bots to understand.
 *<P>
 * Server v2.0.00 through v2.4.00 didn't send that {@code SOCGameState(WAITING_FOR_DISCARDS)},
 * to be a bit more efficient. So for compatibility, server won't send that redundant message to
 * clients older than v2.5 ({@link #VERSION_FOR_ALWAYS_SEND_GAMESTATE}).
 * All client versions including v1.x.xx correctly display progress of the discards
 * without needing that {@code SOCGameState}.
 *
 * @author Robert S. Thomas
 */
public class SOCDiscard extends SOCMessage
    implements SOCMessageForGame
{
    private static final long serialVersionUID = 1111L;  // last structural change v1.1.11

    /**
     * First server version (v2.5.00) where after a player discards, if other players still must discard,
     * server sends {@link SOCGameState}({@link SOCGame#WAITING_FOR_DISCARDS}) for clarity
     * and to mark end of message sequence, although state hasn't changed.
     *<P>
     * That redundant {@code SOCGameState} was also sent in v1.x, but not v2.0 - v2.4.
     *
     * @since 2.5.00
     */
    public static final int VERSION_FOR_ALWAYS_SEND_GAMESTATE = 2500;

    /**
     * Name of game
     */
    private String game;

    /**
     * The set of resources being discarded
     */
    private SOCResourceSet resources;

    /**
     * Create a Discard message.
     *
     * @param ga  the name of the game
     * @param cl  the amount of clay being discarded
     * @param or  the amount of ore being discarded
     * @param sh  the amount of sheep being discarded
     * @param wh  the amount of wheat being discarded
     * @param wo  the amount of wood being discarded
     * @param uk  the amount of unknown resources being discarded
     */
    public SOCDiscard(String ga, int cl, int or, int sh, int wh, int wo, int uk)
    {
        messageType = DISCARD;
        game = ga;
        resources = new SOCResourceSet(cl, or, sh, wh, wo, uk);
    }

    /**
     * Create a Discard message.
     *
     * @param ga  the name of the game
     * @param rs  the resources being discarded
     */
    public SOCDiscard(String ga, int pn, SOCResourceSet rs)
    {
        messageType = DISCARD;
        game = ga;
        resources = rs;
    }

    /**
     * @return the name of the game
     */
    public String getGame()
    {
        return game;
    }

    /**
     * @return the set of resources being discarded
     */
    public SOCResourceSet getResources()
    {
        return resources;
    }

    /**
     * DISCARD sep game sep2 clay sep2 ore sep2 sheep sep2
     * wheat sep2 wood sep2 unknown
     *
     * @return the command string
     */
    public String toCmd()
    {
        return toCmd(game, resources);
    }

    /**
     * DISCARD sep game sep2 clay sep2 ore sep2 sheep sep2
     * wheat sep2 wood sep2 unknown
     *
     * @param ga  the name of the game
     * @param rs  the resources being discarded
     * @return the command string
     */
    public static String toCmd(String ga, SOCResourceSet rs)
    {
        return DISCARD + sep + ga + sep2
            + rs.getAmount(SOCResourceConstants.CLAY) + sep2
            + rs.getAmount(SOCResourceConstants.ORE) + sep2
            + rs.getAmount(SOCResourceConstants.SHEEP) + sep2
            + rs.getAmount(SOCResourceConstants.WHEAT) + sep2
            + rs.getAmount(SOCResourceConstants.WOOD) + sep2
            + rs.getAmount(SOCResourceConstants.UNKNOWN);
    }

    /**
     * Parse the command String into a Discard message
     *
     * @param s   the String to parse
     * @return    a Discard message, or null if the data is garbled
     */
    public static SOCDiscard parseDataStr(String s)
    {
        String ga; // the game name
        int cl; // the amount of clay being discarded
        int or; // the amount of ore being discarded
        int sh; // the amount of sheep being discarded
        int wh; // the amount of wheat being discarded
        int wo; // the amount of wood being discarded
        int uk; // the amount of unknown resources being discarded

        StringTokenizer st = new StringTokenizer(s, sep2);

        try
        {
            ga = st.nextToken();
            cl = Integer.parseInt(st.nextToken());
            or = Integer.parseInt(st.nextToken());
            sh = Integer.parseInt(st.nextToken());
            wh = Integer.parseInt(st.nextToken());
            wo = Integer.parseInt(st.nextToken());
            uk = Integer.parseInt(st.nextToken());
        }
        catch (Exception e)
        {
            return null;
        }

        return new SOCDiscard(ga, cl, or, sh, wh, wo, uk);
    }

    /**
     * Strip out the parameter/attribute names from {@link #toString()}'s format,
     * returning message parameters as a comma-delimited list for {@link SOCMessage#parseMsgStr(String)}.
     * @param message Params part of a message string formatted by {@link #toString()}; not {@code null}
     * @return Message parameters without attribute names, or {@code null} if params are malformed
     * @since 2.5.00
     */
    public static String stripAttribNames(String message)
    {
        message = message.replace("resources=",  "");

        return SOCMessage.stripAttribNames(message);
    }

    /**
     * @return a human readable form of the message
     */
    public String toString()
    {
        return "SOCDiscard:game=" + game + "|resources=" + resources;
    }

}
