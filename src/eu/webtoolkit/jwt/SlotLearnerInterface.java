package eu.webtoolkit.jwt;

import java.io.IOException;

interface SlotLearnerInterface {
	String learn(AbstractEventSignal.LearningListener slot) throws IOException;
}
