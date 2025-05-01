package com.smanzana.petcommand.client.widget;

import java.util.function.Supplier;

import com.smanzana.petcommand.api.pet.PetInfo.PetValue;
import com.smanzana.petcommand.client.widgetdupe.LabeledWidget;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class PetValueWidget extends LabeledWidget {

	public PetValueWidget(Screen parent, int x, int y, int width, int height, Supplier<PetValue> valueSupplier) {
		super(parent, new PetValueLabel(valueSupplier), new PetValueValue(valueSupplier), x, y, width, height);
	}
	
	protected static final class PetValueLabel extends TextLabel {

		protected final Supplier<PetValue> valueSupplier;
		
		public PetValueLabel(Supplier<PetValue> valueSupplier) {
			super(TextComponent.EMPTY);
			this.valueSupplier = valueSupplier;
		}
		
		@Override
		protected Component getLabel() {
			final PetValue value = this.valueSupplier.get();
			if (value != null && value.label() != null) {
				return value.label().copy().append(": ");
			}
			
			return TextComponent.EMPTY;
		}
		
	}
	
	protected static final class PetValueValue extends TextValue {

		public PetValueValue(Supplier<PetValue> valueSupplier) {
			super(() -> getValueString(valueSupplier.get()));
		}
		
		protected static String getValueString(PetValue value) {
			if (value == null) {
				return "";
			}
			
			return value.getFormattedString();
		}
		
	}

}
