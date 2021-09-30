<#assign entity = generator.map(field$entity, "entities", 1)!"null">
<#if entity != "null">
if(world instanceof ServerLevel _level) {
	<#if !field$entity?starts_with("CUSTOM:")>
	Entity entityToSpawn = new ${generator.map(field$entity, "entities", 0)}(${entity}, _level);
   	<#else>
	Entity entityToSpawn = new ${entity}(${JavaModName}EntityTypes.${generator.getRegistryNameForModElement(field$entity?replace("CUSTOM:", ""))?upper_case}, _level);
   	</#if>
	entityToSpawn.moveTo(${input$x}, ${input$y}, ${input$z}, (float) ${input$yaw}, (float) ${input$pitch});
	entityToSpawn.setYBodyRot((float) ${input$yaw});
	entityToSpawn.setYHeadRot((float) ${input$yaw});

	if (entityToSpawn instanceof Mob _mobToSpawn)
		_mobToSpawn.finalizeSpawn(_level, world.getCurrentDifficultyAt(entityToSpawn.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);

	world.addFreshEntity(entityToSpawn);
}
</#if>