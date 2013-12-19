package eu.webtoolkit.jwt;

/**
 * A single precision floating point 2 by 2 matrix. Primarily to support 3D rotations.
 */
public class Matrix2f {
	private float[] data;
	
	/**
	 * Constructs and initializes a Matrix2f to all zeros.
	 */
	public Matrix2f() {
		data = new float[4];
	}
	
	/**
	 * Retrieves the value at the specified row and column of this matrix. 
	 * 
	 * @param row the row number to be retrieved (zero indexed)
	 * @param column the column number to be retrieved (zero indexed)
	 * @return the value at the indexed element
	 */
	public float getElement(int row, int column) {
		return data[row*2 + column];
	}
	
	/**
	 * Sets the specified element of this matrix4f to the value provided.
	 * 
     * @param row the row number to be modified (zero indexed)
     * @param column the column number to be modified (zero indexed)
     * @param value the new value
	 */
	public void setElement(int row, int column, float value) {
		data[row*2 + column] = value;
	}

	/** 
	 * Sets the value of this matrix to the transpose of the argument matrix. 
	 *
	 * @param m1 the matrix to be transposed
	 */
	public void transpose(Matrix2f m1) {
		data[0] = m1.data[0];
		data[1] = m1.data[2];
		data[2] = m1.data[1];
		data[3] = m1.data[3];
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[[" + data[0] + "," + data[1] + "],[" + data[2] + "," + data[3] + "]]";
	}
}
