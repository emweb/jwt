package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.WAbstractTableModel;
import eu.webtoolkit.jwt.WModelIndex;

/**
 * A model that lists the different values of an Enum.
 * 
 * This model is often used in conjunction with a {@link WSelectionBox} or {@link WComboBox} to allow
 * a user to make a choice of a possible value of an enumerated type.
 * 
 * The model has one column, and as many rows as the enum has values.
 * 
 * As {@link ItemDataRole#DisplayRole}, the {@link Enum#toString()} function is returned while
 * as {@link ItemDataRole#UserRole}, the actual Enum value is returned.
 */
public class WEnumModel<E extends Enum<E>> extends WAbstractTableModel {
	private Class<E> enumType;

	/**
	 * Constructor.
	 * 
	 * @param enumType the enum class (e.g. MyEnum.class).
	 */
	public WEnumModel(Class<E> enumType) {
		this.enumType = enumType;
	}
	
	@Override
	public int getColumnCount(WModelIndex parent) {
		if (parent == null)
			return 1;
		else
			return 0;
	}

	@Override
	public Object getData(WModelIndex index, int role) {
		if (role == ItemDataRole.DisplayRole) {
			return enumType.getEnumConstants()[index.getRow()].toString();
		} else if (role == ItemDataRole.UserRole)
			return getObject(index.getRow());
		else
			return null;
	}

	@Override
	public int getRowCount(WModelIndex parent) {
		if (parent == null) {
			return enumType.getEnumConstants().length;
		} else
			return 0;
	}

	/**
	 * Returns the enum value corresponding to a row.
	 * 
	 * @param row
	 * @return the enum value corresponding to a row.
	 * 
	 * @see #getRow(Enum)
	 */
	public E getObject(int row) {
		return enumType.getEnumConstants()[row];
	}

	/**
	 * Returns the row corresponding to an enum value.
	 * 
	 * @param value enum value
	 * @return the row of that value.
	 * 
	 * @see #getObject(int)
	 */
	public int getRow(E value) {
		return value.ordinal();
	}
}
