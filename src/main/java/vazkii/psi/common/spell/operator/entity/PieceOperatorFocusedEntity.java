/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [15/02/2016, 19:14:10 (GMT)]
 */
package vazkii.psi.common.spell.operator.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.common.spell.operator.vector.PieceOperatorVectorRaycast;

import java.util.List;

public class PieceOperatorFocusedEntity extends PieceOperator {

	SpellParam target;

	public PieceOperatorFocusedEntity(Spell spell) {
		super(spell);
	}

	@Override
	public void initParams() {
		addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		Entity e = this.getParamValue(context, target);

		if(e == null)
			throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);

		Entity looked = getEntityLookedAt(e);
		if(looked == null)
			throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);

		return looked;
	}

	public static Entity getEntityLookedAt(Entity e) {
		Entity foundEntity = null;

		final double finalDistance = 32;
		double distance = finalDistance;
		RayTraceResult pos = PieceOperatorVectorRaycast.raycast(e, finalDistance);
		Vec3d positionVector = e.getPositionVector();
		if(e instanceof PlayerEntity)
			positionVector = positionVector.add(0, e.getEyeHeight(), 0);

		if(pos != null)
			distance = pos.hitVec.distanceTo(positionVector);

		Vec3d lookVector = e.getLookVec();
		Vec3d reachVector = positionVector.add(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance);

		Entity lookedEntity = null;
		List<Entity> entitiesInBoundingBox = e.getEntityWorld().getEntitiesWithinAABBExcludingEntity(e, e.getEntityBoundingBox().grow(lookVector.x * finalDistance, lookVector.y * finalDistance, lookVector.z * finalDistance).grow(1F, 1F, 1F));
		double minDistance = distance;

		for(Entity entity : entitiesInBoundingBox) {
			if(entity.canBeCollidedWith()) {
				float collisionBorderSize = entity.getCollisionBorderSize();
				AxisAlignedBB hitbox = entity.getEntityBoundingBox().grow(collisionBorderSize, collisionBorderSize, collisionBorderSize);
				RayTraceResult interceptPosition = hitbox.calculateIntercept(positionVector, reachVector);

				if(hitbox.contains(positionVector)) {
					if(0.0D < minDistance || minDistance == 0.0D) {
						lookedEntity = entity;
						minDistance = 0.0D;
					}
				} else if(interceptPosition != null) {
					double distanceToEntity = positionVector.distanceTo(interceptPosition.hitVec);

					if(distanceToEntity < minDistance || minDistance == 0.0D) {
						lookedEntity = entity;
						minDistance = distanceToEntity;
					}
				}
			}

			if(lookedEntity != null && (minDistance < distance || pos == null))
				foundEntity = lookedEntity;
		}

		return foundEntity;
	}

	@Override
	public Class<?> getEvaluationType() {
		return Entity.class;
	}

}
