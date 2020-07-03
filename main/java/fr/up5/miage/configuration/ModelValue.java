package fr.up5.miage.configuration;

/**
 * This class represents the concept of value in the notation configuration file
 */
public class ModelValue{

	/**
	 * This variable stores the first value
	 */
	private Float firstValue;

	/**
	 * This variable stores the second value if there is one existing
	 */
	private Float secondValue;

	/**
	 * This variable stores the third value if there is one existing
	 */
	private Float thirdValue;

	/**
	 * Constructor of the class that expects one parameter
	 * @param firstValue is the first value of the model value
	 */
	public ModelValue(Float firstValue){
		this.firstValue = firstValue;
	}

	/**
	 * Overload of the constructor class that expects two parameters
	 * @param firstValue is the first value of the model value
	 * @param secondValue is the second value of the model value
	 */
	public ModelValue(Float firstValue, Float secondValue){
		this.firstValue = firstValue;
		this.secondValue = secondValue;
	}

	/**
	 * Overload of the constructor class that expects three parameters
	 * @param firstValue is the first value of the model value
	 * @param secondValue is the second value of the model value
	 * @param thirdValue is the third value of the model value
	 */
	public ModelValue(Float firstValue, Float secondValue, Float thirdValue){
		this.firstValue = firstValue;
		this.secondValue = secondValue;
		this.thirdValue = thirdValue;
	}

	/**
	 * Getter of the firstValue attribute
	 * @return a Double that represents the first value
	 */
	public Float getFirstValue(){
		return firstValue;
	}

	/**
	 * Getter of the secondValue attribute
	 * @return a Double that represents the second value
	 */
	public Float getSecondValue(){
		return secondValue;
	}

	/**
	 * Getter of the thirdValue attribute
	 * @return a Double that represents the third value
	 */
	public Float getThirdValue(){
		return thirdValue;
	}

	@Override
	public String toString() {
		return "ModelValue [firstValue=" + firstValue + ", secondValue=" + secondValue + ", thirdValue=" + thirdValue
				+ "]";
	}
	
	
}
