/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta;

import org.meta.api.common.exceptions.MetaException;
import org.meta.api.configuration.exceptions.InvalidConfigurationException;
import org.meta.api.configuration.exceptions.InvalidConfigurationFileException;
import org.meta.configuration.MetaConfiguration;
import org.meta.controller.MetaController;
import org.meta.plugin.MetaPluginLoader;
import org.meta.plugin.exceptions.PluginLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just the main class.
 *
 * @author nico
 * @version $Id: $
 */
public final class JMeta {

    private static final Logger logger = LoggerFactory.getLogger(JMeta.class);

    private JMeta() {
    }

    /**
     * <p>main</p>
     *
     * @param args Do we really need to describe this?
     * @throws org.meta.api.common.exceptions.MetaException if any.
     */
    public static void main(final String[] args) throws MetaException {
        try {
            logger.info("Reading configuration files");
            MetaConfiguration.initConfiguration();
        } catch (InvalidConfigurationFileException | InvalidConfigurationException ex) {
            logger.error("Failed to initialize configuration from files.", ex);
            return;
        }
        logger.info("Starting JMeta");
        MetaController controller = new MetaController();
        try {
            controller.initAndStartAll();
        } catch (final MetaException ex) {
            logger.error("Failed to start JMeta!", ex);
            controller.close();
            System.exit(-1);
        }
        logger.info("Loading plugins...");
        MetaPluginLoader pluginLoader = new MetaPluginLoader(MetaConfiguration.getPluginsConfiguration(),
                controller);
        try {
            pluginLoader.loadPlugins();
        } catch (PluginLoadException ex) {
            logger.error("Failed to load plugins!", ex);
        }
        logger.info("JMeta started!");
    }
}

//
//
//                             . ...
//                         .''.' .    '.
//                    . '' ".'.:I:.'..  '.
//                  .'.:.:..,,:II:'.'.'.. '.
//                .':.'.:.:I:.:II:'.'.'.'.. '.
//              .'.'.'.'::.:.:.:I:'.'.'.'. .  '
//             ..'.'.'.:.:I::.:II:.'..'.'..    .
//            ..'.'':.:.::.:.::II::.'.'.'.'..   .
//           ..'.'.'.:.::. .:::II:..'.'.'.'.'.   .
//          .':.''.':'.'.'.:.:I:'.'.'.'.'.. '..  ..
//          ':. '.':'. ..:.::.::.:.'..'  ':.'.'.. ..
//         .:.:.':'.   '.:':I:.:.. .'.'.  ': .'.. . ..
//         '..:.:'.   .:.II:.:..   . .:.'. '.. '. .  ..
//        .. :.:.'.  .:.:I:.:. .  . ..:..:. :..':. .  '.
//       .:. :.:.   .:.:I:.:. .    . ..:I::. :: ::  .. ..
//       .. :'.'.:. .:.:I:'.        ..:.:I:. :: ::.   . '.
//       '..:. .:.. .:II:'         ,,;IIIH.  ::. ':.      .
//      .:.::'.:::..:.AII;,      .::",,  :I .::. ':.       .
//      :..:'.:II:.:I:  ,,;'   ' .;:FBT"X:: ..:.. ':.    . .
//     .. :':III:. :.:A"PBF;.  . .P,IP;;":: :I:..'::. .    ..
//     . .:.:II: A.'.';,PP:" .  . ..'..' .: :.::. ':...  . ..
//     . .: .:IIIH:.   ' '.' .  ... .    .:. :.:.. :...    .'
//     . .I.::I:IIA.        ..   ...    ..::.'.'.'.: ..  . .
//      .:II.'.':IA:.      ..    ..:.  . .:.: .''.'  ..  . .
//     ..::I:,'.'::A:.  . .:'-, .-.:..  .:.::AA.. ..:.' .. .
//      ':II:I:.  ':A:. ..:'   ''.. . : ..:::AHI: ..:..'.'.
//     .':III.::.   'II:.:.,,;;;:::::". .:::AHV:: .::'' ..
//     ..":IIHI::. .  "I:..":;,,,,;;". . .:AII:: :.:'  . .
//     . . IIHHI:..'.'.'V::. ":;;;"   ...:AIIV:'.:.'  .. .
//      . . :IIHI:. .:.:.V:.   ' ' . ...:HI:' .:: :. .  ..
//      . .  ':IHII:: ::.IA..      .. .A .,,:::' .:.    .
//      :.  ...'I:I:.: .,AHHA, . .'..AHIV::' . .  :     ..
//      :. '.::::II:.I:.HIHHIHHHHHIHHIHV:'..:. .I.':. ..  '.
//   . . .. '':::I:'.::IHHHHHHHHMHMHIHI. '.'.:IHI..  '  '  '.
//    ':... .  ''" .::'.HMHI:HHHHMHHIHI. :IIHHII:. . . .    .
//     :.:.. . ..::.' .IV".:I:IIIHIHHIH. .:IHI::'.': '..  .  .
//   . .:.:: .. ::'.'.'..':.::I:I:IHHHIA.'.II.:...:' .' ... . '..
//  '..::::' ...::'.IIHII:: .:.:..:..:III:.'::' .'    .    ..  . .
//  '::.:' .''     .. :IIHI:.:.. ..: . .:I:"' ...:.:.  ..    .. ..
//     .:..::I:.  . . . .IHII:.:'   .. ..".::.:II:.:. .  ...   . ..
//  .. . .::.:.,,...-::II:.:'    . ...... . .. .:II:.::  ...  .. ..
//   ..:.::.I .    . . .. .:. .... ...:.. . . ..:.::.   :..   . ..
//    .'.::I:.      . .. ..:.... . ..... .. . ..::. .. .I:. ..' .
//  .'':.: I.       . .. ..:.. .  . .. ..... .:. .:.. .:I.'.''..
//  . .:::I:.       . . .. .:. .    .. ..  . ... .:.'.'I'  .  ...
//  . ::.:I:..     . . . ....:. . .   .... ..   .:...:.:.:. ''.''
//  '.'::'I:.       . .. ....:. .     .. . ..  ..'  .'.:..:..    '
//        :. .     . .. .. .:.... .  .  .... ...   .  .:.:.:..    '.
//        :.      .  . . .. .:.... . . ........       .:.:.::. .    .
//        :. .     . . . . .. .::..:  . ..:.. .        ::.:.:.. .    .
//        :.. .    . . .  . .. ..:.:  .. .. .:. ..     ':::.::.:. .   .
//        ':.. .  . . . .. .. ...::' .. ..  . .:. .     V:I:::::.. .   :.
//         ::. .  . .. .. ... .:.::  .. .  . .. .. .     VI:I:::::..   ''B
//          :.. .   . .. ..:.. ..I:... . .  . .. ... .    VII:I:I:::. .'::
//          ':.. . . . .. ..:..:.:I:.:. .  . .. . .:. .    VHIII:I::.:..':
//           ::..   . . .. ..:..:.HI:. .      . . .... .   :HHIHIII:I::..:
//           ':. .  . .. .. ..:.:.:HI:.    . . .. ..... .   HHHHIHII:I::.'
//            :.. .  . . .. .:.:.:.HI:.      . . .. ... .   IHHHHIHHIHI:'
//             :..  .  . . .. ..:..IH:.     . . .. .. ,,, . 'HHHHHHHHI:'
//             ':..   . . .. ..:.:.:HI..   .  . .. . :::::.  MIH:"""'
//              :. . .  . .. ..::.:.VI:.     . . .. .:::'::. HIH
//               :..  .  . .. .:.:.:.V:.    . . . ...::I"A:. HHV
//                :. .  .  . .. ..:.:.V:.     . . ....::I::'.HV:
//                 :. .  . . . .. .:..II:.  . . . ....':::' AV.'
//                  :.. . . .. ... .:..VI:. . . .. .:. ..:.AV'.
//                  ':.. . .  .. ..:.:.:HAI:.:...:.:.:.:.AII:.
//                   I:. .  .. ... .:.:.VHHII:..:.:..:A:'.:..
//                   IA..  . . .. ..:.:.:VHHHHIHIHHIHI:'.::.
//                   'HA:.  . . .. ..:.:.:HHHIHIHHHIHI:..:.
//                    HIA: .  . . .. ...:.VHHHIHIIHI::.:...
//                    HIHI:. .  .. ... .::.HHHIIHIIHI:::..
//                    HII:.:.  .  .. ... .::VHHIHI:I::.:..
//                    AI:..:..  .  . .. ..:.VHIII:I::.:. .
//                   AI:. ..:..  .  . .. ..' VHIII:I;... .
//                  AI:. .  .:.. .  .  . ...  VHIII::... .
//                .A:. .      :.. .  . .. .:.. VHII::..  .
//               A:. . .       ::. .. .. . .:.. "VHI::.. .
//             .:.. .  .        :.. .:..... .::.. VHI:..
//            ... . .  .     . . :.:. ..:. . .::.. VI:..  .
//           .. .. .  .    . . ...:... . .. . .:::. V:..  .
//          '.. ..  .   .  .. ..:::.... .:. . ..::.. V..  .
//        . . .. . .   . . .. ..:::A. ..:. . . .::.. :..
//       . .. .. .. . .  . ... ..::IA.. .. . .  ..::. :..  .
//      .. .. ... . .  .. .... .:.::IA. . .. . ..:.::. :.  .
//     . . . .. .   . . .. ..:..:.::IIA. . .  .. .:.::. :. .
//    .. . .  .   . . .. ... ..:.::I:IHA. .  . . ..:.::. . .
//   .: ..  .  .   . . ... .:.. .:I:IIHHA. .  . .. .::I:. .
//  .::.  .     . . .. ..:. .::.:IIHIIHHHA.  .  .. ..:I:. . .
//  A::..      .  .  ...:..:.::I:IHIHIHHHHA.  .  . ..::I:. .
// :HI:.. .       . .. .:.:.::I:IHIHIIHIHHHA. .   .. .::I:. ..
// AI:.. .. .    . .. .:.:.::II:IHIIIHIHIHHHA.  .  . ..::I:. ..
//:HI:.. . .   .  . .. .::.:I:IHIHIIIHIHIIHHHA..  . .. .::I:. ..
//AI:.:.. .  .  .  ... .::.::I:IHIIHIHIHIHIHIHHA. .  . ..::I:. .
//HI:. .. . .  .  . .. .:..::IIHIHIHIIIIWHIIHHMWA.  . . .:::I:. . .
//HI:.. . .  .   . .. ..:.::I:IIHHIIHIHIHIHHMMW"  '.. . ..:::II: . .
//HI::.. .  .   .  .. .:..:::IIHIHIIWIWIIWMWW" .    .. . ..::III: .  .
//HI::... . . .  . ... ..:.:::IIHIWIWIWMWMWW. .  .   . .. .:.:III. .   .
//II::.:.. . .  .  .. ......:..IHWHIWWMWMW".. . . . . '... .:.:IHI:..    .
//II:I::.. .  .   .  . .....::.:IHWMWWWMW:.. .  .  . .  .:..:::IIHII..
//:II:.:.:.. .  .   . ......:.:.:IWWMWWW:.:.. .  .  .  . :...:.:IHHI:..
// HI::.:. . . .  .  . ...:.::.::.VWMWW::.:.:.. .  . .. . :.. ..:IHHI::.'-
// HII::.:.. .  .  . .. .:..:.'.  'WWWI::.::.:.. . .  . .. ':...:II:IIII::
// III::.:... .  .  . ...:.:... .   WII:I::.:.. .  .  .. . . :.:::...::.::
//  VII::.:.. . . . .. ...:....      VHI:I::.:.. .  . ... .. .::.:..:.:..:
//   VII::.:.. . .  . ..:.::.. .     :HHII:I::.:.. . . .. ..  .'::':......
//   III:I::.. .. . . .. .:.:.. .    :VHIHI:I::.:... . . .. .. .':. .. .AH
//  AA:II:I::.. . . .  .. ..:.. . .  ::HHIHII:I::.:... .. .. ... .:.::AHHH
// AHH:I:I::.:.. .  . .. ..:.:.. .   ::VHHHVHI:I::.:.:.. ..:. .::.A:.AHHHM
// HHHAII:I::.:.. . . . .. ..:.. . . :::HIHIHIHII:I::.:.. .. .:. ..AHHMMM:
//AHHHH:II:I::.:.. . . .. ..:.:.. . .:I:MMIHHHIHII:I:::.:. ..:.:.AHHHMMM:M
//HHHHHA:II:I::.. .. . . .. .:... . .:IIVMMMHIHHHIHII:I::. . .. AHHMMMM:MH
//HHHHHHA:I:I:::.. . . . ... ..:.. ..:IHIVMMHHHHIHHHIHI:I::. . AHMMMMM:HHH
//HHHHHMM:I::.:.. . . . .. ...:.:...:IIHHIMMHHHII:.:IHII::.  AHMMMMMM:HHHH
//HHHHHMMA:I:.:.:.. . . . .. ..:.:..:IIHHIMMMHHII:...:::.:.AHMMMMMMM:HHHHH
//HHHHHMMMA:I::... . . . . .. ..:.::.:IHHHIMMMHI:.:.. .::AHMMMMMMM:HHHHHHH
//VHHHHMMMMA:I::.. . .  . . .. .:.::I:IHHHIMMMMHI:.. . AHMMMMMMMM:HHHHHHHH
// HHHMMMMMM:I:.:.. . .  . . ...:.:IIHIHHHIMMMMMHI:.AHMMMMMMMMM:HHHHHHHHHH
// HHHHMMMMMA:I:.:.. .  .  . .. .:IIHIHHHHIMMMMMH:AMMMMMMMMMMM:HHHHHHHHHHH
// VHHHMMMMMMA:I:::.:. . . . .. .:IHIHHHHHIMMMV"AMMMMMMMMMMMM:HHHHHHHHHHHH
//  HHHHHMMMMMA:I::.. .. .  . ...:.:IHHHHHHIM"AMMMMMMMMMMMM:HHHHHHHHHHHHHH
//  VHHHHHMMMMMA:I:.:.. . . .  .. .:IHIHHHHI:AMMMMMMMMMMMIHHHHHHHHHHHHHHHH
//   VHHHHHMMMMMA:I::.:. . .  .. .:.:IHHHV:MMMMMIMMMMMMMMMMMMMHHHHHHHHV::.
//    VHHHHMMMMMMA:::.:..:.. . .. .:::AMMMMMMMM:IIIIIHHHHHHHHHHHHHHHV:::..
//     HHHHHMMMIIIA:I::.:.:..:... AMMMMMMMMMM:IIIIIIHHHHHHHHHHHHHHHV::::::
//     VHHHHMMIIIIMA:I::::.::..AMMMMMMMMMMM:IIIIIIIHHHHHHHHHHHHHHV::::::::
//      HHHHMIIIIMMMA:II:I::AIIIMMMMMMMMMM:IIIIIIIHHHHHHHHHHHHHHV:::::::::
//      VHHHHIIIMMMMMMA:I:AIIIIIIMMMMMM:IIIIIIIIHHHHHHHHHHHHHHV::::::::"'
//       HHHHHIIMMMMMMIMAAIIIIIIIIMMM:IIIIIIIIHHHHHHHHHHHHHHHV:::::""'
//       VHHHIIIIMMMMIIIIIIIIIIIIII:IIIIIIIIHHHHHHHHHHHHHHHV::""'
//        VHHIIIMMMMMIIIIIIIIIIIIIIIIIIIIIHHHHHHHHHHHHHHHV
//         VHHIMMMMMMMIIIIIIIIIIIIIIIIIHHHHHHHHHHHHHV
//          VHHHMMMMMMMMIIIIIIIIIIIHHHHHHHHHHHV
//           VHHHMMMMMMMMMMMMMHHHHHHHHHHHHHV
