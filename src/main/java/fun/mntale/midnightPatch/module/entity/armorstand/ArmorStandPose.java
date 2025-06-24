package fun.mntale.midnightPatch.module.entity.armorstand;

import org.bukkit.util.EulerAngle;

public record ArmorStandPose(EulerAngle head, EulerAngle body, EulerAngle leftArm, EulerAngle rightArm,
                             EulerAngle leftLeg, EulerAngle rightLeg) {
}