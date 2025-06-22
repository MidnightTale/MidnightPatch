package fun.mntale.midnightPatch.module.entity.armorstand;

import org.bukkit.util.EulerAngle;
import java.util.HashMap;
import java.util.Map;

public class ArmorStandPosePresets {
    public static Map<String, ArmorStandPose> getPresets() {
        Map<String, ArmorStandPose> presetPoses = new HashMap<>();
        presetPoses.put("default", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0), new EulerAngle(Math.toRadians(-10), 0, 0),
            new EulerAngle(Math.toRadians(-10), 0, 0), new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
        presetPoses.put("sitting", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(5)),
            new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(-5)),
            new EulerAngle(Math.toRadians(90), 0, 0),
            new EulerAngle(Math.toRadians(90), 0, 0)
        ));
        presetPoses.put("wave", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-120), Math.toRadians(0), Math.toRadians(-45)),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
        presetPoses.put("victory", new ArmorStandPose(
            new EulerAngle(Math.toRadians(-5), 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-150), Math.toRadians(0), Math.toRadians(-35)),
            new EulerAngle(Math.toRadians(-150), Math.toRadians(0), Math.toRadians(35)),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
        presetPoses.put("bow", new ArmorStandPose(
            new EulerAngle(Math.toRadians(35), 0, 0),
            new EulerAngle(Math.toRadians(25), 0, 0),
            new EulerAngle(Math.toRadians(-10), 0, 0), new EulerAngle(Math.toRadians(-10), 0, 0),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
        presetPoses.put("superhero", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(Math.toRadians(20), 0, 0),
            new EulerAngle(Math.toRadians(-70), 0, 0),
            new EulerAngle(Math.toRadians(-90), 0, 0),
            new EulerAngle(Math.toRadians(15), 0, 0),
            new EulerAngle(Math.toRadians(-15), 0, 0)
        ));
        presetPoses.put("thinking", new ArmorStandPose(
            new EulerAngle(Math.toRadians(-10), Math.toRadians(0), Math.toRadians(20)),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-100), Math.toRadians(-20), 0),
            new EulerAngle(Math.toRadians(-15), 0, Math.toRadians(-5)),
            new EulerAngle(Math.toRadians(0), Math.toRadians(0), Math.toRadians(15)),
            new EulerAngle(Math.toRadians(0), Math.toRadians(0), Math.toRadians(-15))
        ));
        presetPoses.put("facepalm", new ArmorStandPose(
            new EulerAngle(Math.toRadians(25), 0, 0),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-160), Math.toRadians(25), 0),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
        presetPoses.put("shrug", new ArmorStandPose(
            new EulerAngle(Math.toRadians(5), 0, 0),
            new EulerAngle(0, 0, 0),
            new EulerAngle(Math.toRadians(-20), Math.toRadians(0), Math.toRadians(25)),
            new EulerAngle(Math.toRadians(-20), Math.toRadians(0), Math.toRadians(-25)),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
        presetPoses.put("t-pose", new ArmorStandPose(
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0),
            new EulerAngle(0, 0, Math.toRadians(90)),
            new EulerAngle(0, 0, Math.toRadians(-90)),
            new EulerAngle(0, 0, 0), new EulerAngle(0, 0, 0)
        ));
        return presetPoses;
    }
} 