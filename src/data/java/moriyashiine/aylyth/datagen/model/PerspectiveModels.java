package moriyashiine.aylyth.datagen.model;

import net.minecraft.client.render.model.json.ModelTransformationMode;

public class PerspectiveModels {
    public static final PerspectiveModelData BIG_HANDHELD = PerspectiveModelData.builder(ModelKeys.GUI)
            .withModel(ModelTransformationMode.FIRST_PERSON_LEFT_HAND, ModelKeys.HANDHELD)
            .withModel(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, ModelKeys.HANDHELD)
            .withModel(ModelTransformationMode.THIRD_PERSON_LEFT_HAND, ModelKeys.HANDHELD)
            .withModel(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, ModelKeys.HANDHELD)
            .build();
}
