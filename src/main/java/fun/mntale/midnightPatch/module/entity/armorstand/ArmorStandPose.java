package fun.mntale.midnightPatch.module.entity.armorstand;

import org.bukkit.util.EulerAngle;

public class ArmorStandPose {
    public final EulerAngle head, body, leftArm, rightArm, leftLeg, rightLeg;
    public ArmorStandPose(EulerAngle head, EulerAngle body, EulerAngle leftArm, EulerAngle rightArm, EulerAngle leftLeg, EulerAngle rightLeg) {
        this.head = head;
        this.body = body;
        this.leftArm = leftArm;
        this.rightArm = rightArm;
        this.leftLeg = leftLeg;
        this.rightLeg = rightLeg;
    }
} 