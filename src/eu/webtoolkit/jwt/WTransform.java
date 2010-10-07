/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A 2D affine transformation matrix.
 * <p>
 * 
 * The matrix is encoded using 6 parameters: <blockquote>
 * 
 * <pre>
 * m11  m12   0
 *  m21  m22   0
 *  dx   dy    1
 * </pre>
 * 
 * </blockquote>
 * <p>
 * In this representation, {@link WTransform#getDx() getDx()} (=
 * {@link WTransform#getM31() getM31()}) and {@link WTransform#getDy() getDy()}
 * (= {@link WTransform#getM32() getM32()}) represent the translation
 * components, and m<i>xy</i> represent a 2D matrix that contains the scale,
 * rotation (and skew) components.
 * <p>
 * The transformation is used to represent a tansformed coordinate system, and
 * provides methods to {@link WTransform#rotate(double angle) rotate()},
 * {@link WTransform#scale(double sx, double sy) scale()},
 * {@link WTransform#shear(double sh, double sv) shear()} or
 * {@link WTransform#translate(double dx, double dy) translate()} this
 * coordinate system.
 * <p>
 * There are also 2 methods to decompose an arbitrary matrix into elementary
 * operations:
 * <ul>
 * <li>
 * {@link WTransform#decomposeTranslateRotateScaleSkew(WTransform.TRSSDecomposition result)
 * decomposeTranslateRotateScaleSkew()} decomposes into a <i>T</i> &#x2218;
 * <i>R</i> &#x2218; <i>Sxx</i> &#x2218; <i>Sxy</i></li>
 * <li>
 * {@link WTransform#decomposeTranslateRotateScaleRotate(WTransform.TRSRDecomposition result)
 * decomposeTranslateRotateScaleRotate()} decomposes into a <i>T</i> &#x2218;
 * <i>R1</i> &#x2218; <i>Sxx</i> &#x2218; <i>R2</i></li>
 * </ul>
 * <p>
 * with <i>T</i> a translation, <i>R</i> a rotation, <i>Sxx</i> a scale, and
 * <i>Sxy</i> a skew component.
 */
public class WTransform {
	/**
	 * Default constructor.
	 * <p>
	 * Creates the identity transformation matrix.
	 */
	public WTransform() {
		this.reset();
	}

	/**
	 * Construct a custom matrix by specifying the parameters.
	 * <p>
	 * Creates a matrix from the specified parameters.
	 */
	public WTransform(double m11, double m12, double m21, double m22,
			double dx, double dy) {
		this.m_[M11] = m11;
		this.m_[M12] = m21;
		this.m_[M13] = dx;
		this.m_[M21] = m12;
		this.m_[M22] = m22;
		this.m_[M23] = dy;
	}

	/**
	 * Assignment method.
	 * <p>
	 * Copies the transformation from the <code>rhs</code>.
	 */
	public WTransform assign(WTransform rhs) {
		for (int i = 0; i < 6; ++i) {
			this.m_[i] = rhs.m_[i];
		}
		return this;
	}

	/**
	 * Clone method.
	 * <p>
	 * Clones this {@link WTransform} object.
	 */
	public WTransform clone() {
		WTransform result = new WTransform();
		result.assign(this);
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * Returns <code>true</code> if the transforms are exactly the same.
	 */
	public boolean equals(WTransform rhs) {
		for (int i = 0; i < 6; ++i) {
			if (this.m_[i] != rhs.m_[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Identity check.
	 * <p>
	 * Returns true if the transform represents an identity transformation.
	 */
	public boolean isIdentity() {
		return this.m_[M11] == 1.0 && this.m_[M22] == 1.0
				&& this.m_[M21] == 0.0 && this.m_[M12] == 0.0
				&& this.m_[M13] == 0.0 && this.m_[M23] == 0.0;
	}

	/**
	 * Returns the horizontal scaling factor.
	 */
	public double getM11() {
		return this.m_[M11];
	}

	/**
	 * Returns the vertical shearing factor.
	 */
	public double getM12() {
		return this.m_[M21];
	}

	/**
	 * Returns m13 = 0.
	 */
	public double getM13() {
		return 0;
	}

	/**
	 * Returns the horizontal shearing factor.
	 */
	public double getM21() {
		return this.m_[M12];
	}

	/**
	 * Returns the vertical scaling factor.
	 */
	public double getM22() {
		return this.m_[M22];
	}

	/**
	 * Returns m23 = 0.
	 */
	public double getM23() {
		return 0;
	}

	/**
	 * Returns the horizontal translation factor.
	 * <p>
	 * Is equivalent to {@link WTransform#getDx() getDx()}
	 */
	public double getM31() {
		return this.m_[M13];
	}

	/**
	 * Returns the vertical translation factor.
	 * <p>
	 * Is equivalent to {@link WTransform#getDy() getDy()}
	 */
	public double getM32() {
		return this.m_[M23];
	}

	/**
	 * Returns m33 = 1.
	 */
	public double getM33() {
		return 1;
	}

	/**
	 * Returns the horizontal translation factor.
	 * <p>
	 * Is equivalent to {@link WTransform#getM31() getM31()}
	 */
	public double getDx() {
		return this.m_[DX];
	}

	/**
	 * Returns the vertical translation factor.
	 * <p>
	 * Is equivalent to {@link WTransform#getM32() getM32()}
	 */
	public double getDy() {
		return this.m_[DY];
	}

	/**
	 * Applys the transformation to a point.
	 * <p>
	 * Returns the transformed point.
	 * <p>
	 * 
	 * @see WTransform#map(double x, double y, Double tx, Double ty)
	 */
	public WPointF map(WPointF p) {
		double x;
		double y;
		x = this.m_[M11] * p.getX() + this.m_[M12] * p.getY() + this.m_[M13];
		y = this.m_[M21] * p.getX() + this.m_[M22] * p.getY() + this.m_[M23];
		;
		return new WPointF(x, y);
	}

	/**
	 * Applys the transformation to a point.
	 * <p>
	 * Sets the point (<i>tx</i>, <code>ty</code>) to the transformation of the
	 * point (<i>x</i>, <code>y</code>).
	 * <p>
	 * 
	 * @see WTransform#map(WPointF p)
	 */
	public void map(double x, double y, Double tx, Double ty) {
		tx = this.m_[M11] * x + this.m_[M12] * y + this.m_[M13];
		ty = this.m_[M21] * x + this.m_[M22] * y + this.m_[M23];
	}

	/**
	 * Resets the transformation to the identity.
	 * <p>
	 * 
	 * @see WTransform#isIdentity()
	 * @see WTransform#WTransform()
	 */
	public void reset() {
		this.m_[M11] = this.m_[M22] = 1;
		this.m_[M21] = this.m_[M12] = this.m_[M13] = this.m_[M23] = 0;
	}

	/**
	 * Rotates the transformation.
	 * <p>
	 * Applies a clock-wise rotation to the current transformation matrix, over
	 * <code>angle</code> degrees.
	 * <p>
	 * 
	 * @see WTransform#rotateRadians(double angle)
	 */
	public WTransform rotate(double angle) {
		this.rotateRadians(degreesToRadians(angle));
		return this;
	}

	/**
	 * Rotates the transformation.
	 * <p>
	 * Applies a clock-wise rotation to the current transformation matrix, over
	 * <code>angle</code> radians.
	 * <p>
	 * 
	 * @see WTransform#rotate(double angle)
	 */
	public WTransform rotateRadians(double angle) {
		double r11 = Math.cos(angle);
		double r12 = -Math.sin(angle);
		double r21 = -r12;
		double r22 = r11;
		return this.multiplyAndAssign(new WTransform(r11, r21, r12, r22, 0, 0));
	}

	/**
	 * Scales the transformation.
	 * <p>
	 * Applies a clock-wise rotation to the current transformation matrix, over
	 * <code>angle</code> radians.
	 * <p>
	 * 
	 * @see WTransform#shear(double sh, double sv)
	 */
	public WTransform scale(double sx, double sy) {
		return this.multiplyAndAssign(new WTransform(sx, 0, 0, sy, 0, 0));
	}

	/**
	 * Shears the transformation.
	 * <p>
	 * Shears the current transformation.
	 * <p>
	 * 
	 * @see WTransform#scale(double sx, double sy)
	 * @see WTransform#rotate(double angle)
	 */
	public WTransform shear(double sh, double sv) {
		return this.multiplyAndAssign(new WTransform(0, sv, sh, 0, 0, 0));
	}

	/**
	 * Translates the transformation.
	 * <p>
	 * Translates the current transformation.
	 */
	public WTransform translate(double dx, double dy) {
		return this.multiplyAndAssign(new WTransform(1, 0, 0, 1, dx, dy));
	}

	/**
	 * Adds a transform that is conceptually applied after this transform.
	 */
	public WTransform multiplyAndAssign(WTransform Y) {
		WTransform X = this;
		double z11 = X.m_[M11] * Y.m_[M11] + X.m_[M12] * Y.m_[M21];
		double z12 = X.m_[M11] * Y.m_[M12] + X.m_[M12] * Y.m_[M22];
		double z13 = X.m_[M11] * Y.m_[M13] + X.m_[M12] * Y.m_[M23] + X.m_[M13];
		double z21 = X.m_[M21] * Y.m_[M11] + X.m_[M22] * Y.m_[M21];
		double z22 = X.m_[M21] * Y.m_[M12] + X.m_[M22] * Y.m_[M22];
		double z23 = X.m_[M21] * Y.m_[M13] + X.m_[M22] * Y.m_[M23] + X.m_[M23];
		this.m_[M11] = z11;
		this.m_[M12] = z12;
		this.m_[M13] = z13;
		this.m_[M21] = z21;
		this.m_[M22] = z22;
		this.m_[M23] = z23;
		return this;
	}

	/**
	 * Multiply 2 transform objects.
	 */
	public WTransform multiply(WTransform rhs) {
		WTransform result = this;
		return result.multiplyAndAssign(rhs);
	}

	/**
	 * Returns the determinant.
	 */
	public double getDeterminant() {
		return this.getM11()
				* (this.getM33() * this.getM22() - this.getM32()
						* this.getM23())
				- this.getM21()
				* (this.getM33() * this.getM12() - this.getM32()
						* this.getM13())
				+ this.getM31()
				* (this.getM23() * this.getM12() - this.getM22()
						* this.getM13());
	}

	/**
	 * Returns the adjoint.
	 */
	public WTransform getAdjoint() {
		return new WTransform(this.getM33() * this.getM22() - this.getM32()
				* this.getM23(), -(this.getM33() * this.getM12() - this
				.getM32()
				* this.getM13()), -(this.getM33() * this.getM21() - this
				.getM31()
				* this.getM23()), this.getM33() * this.getM11() - this.getM31()
				* this.getM13(), this.getM32() * this.getM21() - this.getM31()
				* this.getM22(), -(this.getM32() * this.getM11() - this
				.getM31()
				* this.getM12()));
	}

	/**
	 * Returns the inverted transformation.
	 * <p>
	 * Returns <code>this</code> if the transformation could not be inverted (
	 * {@link WTransform#getDeterminant() getDeterminant()} == 0), and logs an
	 * error instead.
	 */
	public WTransform getInverted() {
		double det = this.getDeterminant();
		if (det != 0) {
			WTransform adj = this.getAdjoint();
			return new WTransform(adj.getM11() / det, adj.getM12() / det, adj
					.getM21()
					/ det, adj.getM22() / det, adj.getM31() / det, adj.getM32()
					/ det);
		} else {
			WApplication.getInstance().log("error").append(
					"WTransform::inverted(): determinant == 0");
			return this;
		}
	}

	/**
	 * Result of a TRSS decomposition.
	 * <p>
	 * 
	 * @see WTransform#decomposeTranslateRotateScaleSkew(WTransform.TRSSDecomposition
	 *      result)
	 */
	public static class TRSSDecomposition {
		/**
		 * X component of translation.
		 */
		public double dx;
		/**
		 * Y component of translation.
		 */
		public double dy;
		/**
		 * Rotation angle (radians).
		 */
		public double alpha;
		/**
		 * X component of scale.
		 */
		public double sx;
		/**
		 * Y component of scale.
		 */
		public double sy;
		/**
		 * Shear (in Y direction).
		 */
		public double sh;
	}

	/**
	 * Decomposes the transformation.
	 * <p>
	 * Decomposes the transformation into elementary operations: translation
	 * (<i>dx</i>, <code>dy</code>), followed by rotation (<i>alpha</i>),
	 * followed by scale (<i>sx</i>, <code>sy</code>) and vertical shearing
	 * factor (<code>sh</code>). The angle is expressed in radians.
	 * <p>
	 * This performs a <a
	 * href="http://en.wikipedia.org/wiki/Gram_schmidt">Gram-Schmidt
	 * orthonormalization</a>.
	 */
	public void decomposeTranslateRotateScaleSkew(
			WTransform.TRSSDecomposition result) {
		double[] q1 = new double[2];
		double[] q2 = new double[2];
		double r11 = norm(this.m_[M11], this.m_[M21]);
		q1[0] = this.m_[M11] / r11;
		q1[1] = this.m_[M21] / r11;
		double r12 = this.m_[M12] * q1[0] + this.m_[M22] * q1[1];
		double r22 = norm(this.m_[M12] - r12 * q1[0], this.m_[M22] - r12
				* q1[1]);
		q2[0] = (this.m_[M12] - r12 * q1[0]) / r22;
		q2[1] = (this.m_[M22] - r12 * q1[1]) / r22;
		result.alpha = Math.atan2(q1[1], q1[0]);
		result.sx = r11;
		result.sy = r22;
		result.sh = r12 / r11;
		result.dx = this.m_[DX];
		result.dy = this.m_[DY];
	}

	/**
	 * Result of a TRSR decomposition.
	 * <p>
	 * 
	 * @see WTransform#decomposeTranslateRotateScaleRotate(WTransform.TRSRDecomposition
	 *      result)
	 */
	public static class TRSRDecomposition {
		/**
		 * X component of translation.
		 */
		public double dx;
		/**
		 * Y component of translation.
		 */
		public double dy;
		/**
		 * First rotation angle (radians).
		 */
		public double alpha1;
		/**
		 * X component of scale.
		 */
		public double sx;
		/**
		 * Y component of scale.
		 */
		public double sy;
		/**
		 * Second rotation angle (radians).
		 */
		public double alpha2;
	}

	/**
	 * Decomposes the transformation.
	 * <p>
	 * Decomposes the transformation into elementary operations: translation
	 * (<i>dx</i>, <code>dy</code>), followed by rotation (<i>alpha2</i>),
	 * followed by scale (<i>sx</i>, <code>sy</code>) and again a rotation (
	 * <code>alpha2</code>). The angles are expressed in radians.
	 * <p>
	 * This performs a <a
	 * href="http://en.wikipedia.org/wiki/Singular_value_decomposition">Singular
	 * Value Decomposition (SVD)</a>.
	 */
	public void decomposeTranslateRotateScaleRotate(
			WTransform.TRSRDecomposition result) {
		double[] mtm = new double[4];
		matrixMultiply(this.m_[M11], this.m_[M21], this.m_[M12], this.m_[M22],
				this.m_[M11], this.m_[M12], this.m_[M21], this.m_[M22], mtm);
		double[] e = new double[2];
		double[] V = new double[4];
		eigenValues(mtm, e, V);
		result.sx = Math.sqrt(e[0]);
		result.sy = Math.sqrt(e[1]);
		if (V[0] * V[3] - V[1] * V[2] < 0) {
			result.sx = -result.sx;
			V[0] = -V[0];
			V[2] = -V[2];
		}
		double[] U = new double[4];
		matrixMultiply(this.m_[0], this.m_[1], this.m_[2], this.m_[3], V[0],
				V[1], V[2], V[3], U);
		U[0] /= result.sx;
		U[2] /= result.sx;
		U[1] /= result.sy;
		U[3] /= result.sy;
		if (U[0] * U[3] - U[1] * U[2] < 0) {
			result.sx = -result.sx;
			U[0] = -U[0];
			U[2] = -U[2];
		}
		result.alpha1 = Math.atan2(U[2], U[0]);
		result.alpha2 = Math.atan2(V[1], V[0]);
		result.dx = this.m_[DX];
		result.dy = this.m_[DY];
	}

	/**
	 * Utility method to convert degrees to radians.
	 */
	public static double degreesToRadians(double angle) {
		return angle / 180. * 3.14159265358979323846;
	}

	private static final int M11 = 0;
	private static final int M12 = 1;
	private static final int M21 = 2;
	private static final int M22 = 3;
	private static final int M13 = 4;
	private static final int DX = 4;
	private static final int M23 = 5;
	private static final int DY = 5;
	private double[] m_ = new double[6];

	static double norm(double x1, double x2) {
		return Math.sqrt(x1 * x1 + x2 * x2);
	}

	static void matrixMultiply(double a11, double a12, double a21, double a22,
			double b11, double b12, double b21, double b22, double[] result) {
		result[0] = a11 * b11 + a12 * b21;
		result[1] = a11 * b12 + a12 * b22;
		result[2] = a21 * b11 + a22 * b21;
		result[3] = a21 * b12 + a22 * b22;
	}

	static void eigenValues(double[] m, double[] l, double[] v) {
		final double a = m[0];
		final double b = m[1];
		final double c = m[2];
		final double d = m[3];
		double B = -a - d;
		double C = a * d - b * c;
		double Dsqr = B * B - 4 * C;
		if (Dsqr <= 0) {
			Dsqr = 0;
		}
		double D = Math.sqrt(Dsqr);
		l[0] = -(B + (B < 0 ? -D : D)) / 2.0;
		l[1] = -B - l[0];
		if (Math.abs(l[0] - l[1]) < 1E-5) {
			v[0] = 1;
			v[2] = 0;
			v[1] = 0;
			v[3] = 1;
		} else {
			if (Math.abs(c) > 1E-5) {
				v[0] = d - l[0];
				v[2] = -c;
				v[1] = d - l[1];
				v[3] = -c;
			} else {
				if (Math.abs(b) > 1E-5) {
					v[0] = -b;
					v[2] = a - l[0];
					v[1] = -b;
					v[3] = a - l[1];
				} else {
					if (Math.abs(l[0] - a) < 1E-5) {
						v[0] = 1;
						v[2] = 0;
						v[1] = 0;
						v[3] = 1;
					} else {
						v[0] = 0;
						v[2] = 1;
						v[1] = 1;
						v[3] = 0;
					}
				}
			}
		}
		double v1l = Math.sqrt(v[0] * v[0] + v[2] * v[2]);
		v[0] /= v1l;
		v[2] /= v1l;
		double v2l = Math.sqrt(v[1] * v[1] + v[3] * v[3]);
		v[1] /= v2l;
		v[3] /= v2l;
	}
}
