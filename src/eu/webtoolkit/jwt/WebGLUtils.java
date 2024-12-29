package eu.webtoolkit.jwt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.FloatBuffer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.GVector;
import javax.vecmath.GMatrix;

import eu.webtoolkit.jwt.utils.MathUtils;

public class WebGLUtils {
	private static final Logger logger = LoggerFactory.getLogger(WebGLUtils.class);
	
	static String makeFloat(double d) {
		return MathUtils.roundJs(d, 6);
	}
	
	static String makeInt(int i) {
		return String.valueOf(i);
	}
	
	public static java.nio.ByteBuffer newByteBuffer(int capacity) {
		java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(capacity);
		buf.order(java.nio.ByteOrder.nativeOrder());
		return buf;
	}

	static void renderfv(StringWriter js, Matrix4f t, JsArrayType arrayType) {
		if (arrayType == JsArrayType.Float32Array) {
			js.append("new Float32Array([");
		} else if (arrayType == JsArrayType.Array) {
			js.append("[");
		} else {
			return;
		}
	    final int dim = 4;
	    for (int i = 0; i < dim; i++)
	    	for (int j = 0; j < dim; j++)
	    		js.append((i == 0 && j == 0 ? "" : ",")).append(makeFloat(t.getElement(i, j)));
	    if (arrayType == JsArrayType.Float32Array) {
	    	js.append("])");
		} else if (arrayType == JsArrayType.Array) {
			js.append("]");
		}
	    
	}
    
	static void renderfv(StringWriter js, Matrix2f t, JsArrayType arrayType) {
	    if (arrayType == JsArrayType.Float32Array) {
			js.append("new Float32Array([");
		} else if (arrayType == JsArrayType.Array) {
			js.append("[");
		} else {
			return;
		}
	    final int dim = 2;
	    for (int i = 0; i < dim; i++)
	    	for (int j = 0; j < dim; j++)
	    		js.append((i == 0 && j == 0 ? "" : ",")).append(makeFloat(t.getElement(i, j)));
	    if (arrayType == JsArrayType.Float32Array) {
	    	js.append("])");
		} else if (arrayType == JsArrayType.Array) {
			js.append("]");
		}
	}
	
	static void typeOpen(StringWriter js, JsArrayType arrayType) {
		if (arrayType == JsArrayType.Float32Array) {
			js.append("new Float32Array([");
		} else if (arrayType == JsArrayType.Array) {
			js.append("[");
		} else {
			return;
		}
	}
	
	static void typeClose(StringWriter js, JsArrayType arrayType) {
		if (arrayType == JsArrayType.Float32Array) {
	    	js.append("])");
		} else if (arrayType == JsArrayType.Array) {
			js.append("]");
		} else {
			return;
		}
	}
	
	public static void renderfv(StringWriter js, Matrix3f t, JsArrayType arrayType) {
	    typeOpen(js, arrayType);
	    final int dim = 3;
	    for (int i = 0; i < dim; i++)
	    	for (int j = 0; j < dim; j++)
	    		js.append((i == 0 && j == 0 ? "" : ",")).append(makeFloat(t.getElement(i, j)));
	    typeClose(js, arrayType);
	}
	
	static void renderfv(StringWriter js, float[] value, int size, JsArrayType arrayType) {
		typeOpen(js, arrayType);
		for (int i = 0; i < size; i++)
			js.append((i == 0 ? "" : ",")).append(makeFloat(value[i]));
		typeClose(js, arrayType);
	}
	
	static void renderfv(StringWriter js, double[] value, int size, JsArrayType arrayType) {
		typeOpen(js, arrayType);
		for (int i = 0; i < size; i++)
			js.append((i == 0 ? "" : ",")).append(makeFloat(value[i]));
		typeClose(js, arrayType);
	}
	
	public static void renderfv(StringWriter js, FloatBuffer buffer, JsArrayType arrayType) {
		typeOpen(js, arrayType);
		for (int i = 0; i < buffer.capacity(); i++)
			js.append((i == 0 ? "" : ",")).append(makeFloat(buffer.get()));
		typeClose(js, arrayType);
	}

    public static void renderfv(StringWriter js, java.nio.ByteBuffer buffer, JsArrayType arrayType) {
    	buffer.rewind();
    	
    	java.nio.FloatBuffer fb = buffer.asFloatBuffer();
    	typeOpen(js, arrayType);
    	for (int i = 0; i < buffer.capacity(); i++)
    		js.append((i == 0 ? "" : ",")).append(makeFloat(buffer.getFloat()));
    	typeClose(js, arrayType);
	}
    
    public static WMemoryResource rpdToMemResource(WRasterPaintDevice rpd) {
    	ByteArrayOutputStream data = new ByteArrayOutputStream();
    	try {
    		rpd.write(data);
    	} catch (IOException e) {
    		logger.error("rpdToMemResource: unexpected IOException", e);
    	}
    	WMemoryResource mr = new WMemoryResource("image/png");
    	mr.setData(data.toByteArray());
    	return mr;
    }

	static Matrix2f transpose(Matrix2f m) {
		Matrix2f t = new Matrix2f();
		t.transpose(m);
		return t;
	}

	public static Matrix3f transpose(Matrix3f m) {
		Matrix3f t = new Matrix3f();
		t.transpose(m);
		return t;
	}

	public static Matrix4f transpose(Matrix4f m) {
		Matrix4f t = new Matrix4f();
		t.transpose(m);
		return t;
	}
	
	public static void translate(Matrix4f m, double x, double y, double z) {
		Matrix4f t = new Matrix4f(); t.setIdentity();
		t.setTranslation(new Vector3f((float)x, (float)y, (float)z));
		m.mul(m, t);
	}
	
	public static void rotate(Matrix4f m, double angle, double x, double y, double z) {
		Matrix4f t = new Matrix4f(); t.setIdentity();
		t.setRotation(new AxisAngle4f((float)x, (float)y, (float)z, (float)(angle/180.0*3.141592)));
		m.mul(m, t);
	}

	public static void scale(Matrix4f m, float scaling) {
		Matrix4f t = new Matrix4f(); t.setIdentity();
		t.setScale(scaling);
		m.mul(m, t);
	}
	
	 /** Apply a transformation to position a camera
	   *
	   * (eyeX, eyeY, eyeZ) is the position of the camera.
	   *
	   * The camera looks at (centerX, centerY, centerZ).
	   *
	   * (upX, upY, upZ) is a vector that is the direction of the up vector.
	   *
	   * This method applies a rotation and translation transformation to
	   * the current matrix so that the given eye becomes (0, 0, 0), the
	   * center point is on the negative Z axis, and the up vector lies in the
	   * X=0 plane, with its Y component in the positive Y axis direction.
	   *
	   * The up vector must not be parallel to the line between eye and center.
	   * The vectors will be normalized and are not required to be perpendicular.
	   *
	   * If the lookat transformation matrix is M, and the current value of
	   * the Matrix4f matrix is T, the resulting matrix after lookAt returns
	   * will be T * M.
	   */
	public static void lookAt(Matrix4f T, double eyeX, double eyeY, double eyeZ,
	    double centerX, double centerY, double centerZ,
	    double upX, double upY, double upZ)
	{
	    // A 3D vector class would be handy here
	    // Compute and normalize lookDir
	    double lookDirX = centerX - eyeX;
	    double lookDirY = centerY - eyeY;
	    double lookDirZ = centerZ - eyeZ;
	    double lookDirNorm = Math.sqrt(lookDirX*lookDirX + lookDirY*lookDirY + lookDirZ*lookDirZ);
	    lookDirX /= lookDirNorm;
	    lookDirY /= lookDirNorm;
	    lookDirZ /= lookDirNorm;
	    // Compute and normalize the 'side' vector: cross product of lookDir and upDir
	    double sideX = lookDirY*upZ - upY*lookDirZ;
	    double sideY = -(lookDirX*upZ - upX*lookDirZ);
	    double sideZ = lookDirX*upY - upX*lookDirY;
	    double sideNormal = Math.sqrt(sideX*sideX + sideY*sideY + sideZ*sideZ);
	    sideX /= sideNormal;
	    sideY /= sideNormal;
	    sideZ /= sideNormal;
	    // Compute the normalized 'up' vector: cross-prod of normalized look
	    // and side dirs:
	    double upDirX = sideY*lookDirZ - lookDirY*sideZ;
	    double upDirY = -(sideX*lookDirZ - lookDirX*sideZ);
	    double upDirZ = sideX*lookDirY - lookDirX*sideY;
	    Matrix4f l = new Matrix4f(
	      (float)sideX,     (float)sideY,     (float)sideZ,     (float)-(eyeX*sideX + eyeY*sideY + eyeZ*sideZ),
	      (float)upDirX,    (float)upDirY,    (float)upDirZ,    (float)-(eyeX*upDirX + eyeY*upDirY + eyeZ*upDirZ),
	      (float)-lookDirX, (float)-lookDirY, (float)-lookDirZ, (float)+(+eyeX*lookDirX + eyeY*lookDirY + eyeZ*lookDirZ),
	      (float)0.0,      (float)0.0,         (float)0.0,     (float)1.0
	    );
	    
	    T.mul(l,T);
	  }
	  /** Construct a perspective projection matrix
	   *
	   * This function constructs a perspective projection where the
	   * camera is located in the origin. The visible volume is determined
	   * by whatever that is visible when looking from the origin through the
	   * rectangular 'window' defined by the coordinates (l, b, n) and
	   * (r, t, n) (parallel to the XY plane). The zone is further delimited
	   * by the near and the far clipping planes.
	   *
	   * The perspective matrix (P) is right-multiplied with the current
	   * transformation matrix (M): M * P. Usually, you will want M to be
	   * the identity matrix when using this method.
	   */
	  public static void frustum(Matrix4f M, double left, double right, double bottom, double top,
	      double near, double far)
	  {
	    Matrix4f f = new Matrix4f();
	    f.setElement(0, 0, (float)(2 * near / (right - left)));
	    f.setElement(0, 1, 0f);
	    f.setElement(0, 2, (float)((right + left) / (right - left)));
	    f.setElement(0, 3, 0f);
	    
	    f.setElement(1, 0, 0f);
	    f.setElement(1, 1, (float)(2 * near / (top - bottom)));
	    f.setElement(1, 2, (float)((top + bottom) / (top - bottom)));
	    f.setElement(1, 3, 0f);
	    
	    f.setElement(2, 0, 0f);
	    f.setElement(2, 1, 0f);
	    f.setElement(2, 2, (float)(- (far + near) / (far - near)));
	    f.setElement(2, 3, (float)(- 2 * far * near / (far - near)));
	    
	    f.setElement(3, 0,0f);
	    f.setElement(3, 1,0f);
	    f.setElement(3, 2,-1f);
	    f.setElement(3, 3, 0f);
	    M.mul(f, M);
	  }

    /** Create an orhtographic projection matrix for use in OpenGL
     *
     * Create an orthographic projection matrix. The given left, right,
     * bottom, top, near and far points will be linearly mapped to the OpenGL
     * unit cube ((1,1,1) to (-1,-1,-1)).
     *
     * The orthographic matrix (O) is right-multiplied with the current
     * transformation matrix (M): M * O. Usually, you will want M to be
     * the identity matrix when using this method.
     */
    public static void ortho(Matrix4f M, double left, double right, double bottom, double top, double nearPlane, double farPlane)
    {
	Matrix4f transf = new Matrix4f((float)(2.0 / (right - left)), 0.0f, 0.0f, (float)(-(right + left) / (right - left)),
				       0.0f, (float)(2.0 / (top - bottom)), 0.0f, (float)(- (top + bottom) / (top - bottom)),
				       0.0f, 0.0f, (float)(-2.0 / (farPlane - nearPlane)), (float)(- (farPlane + nearPlane) / (farPlane - nearPlane)),
				       0.0f, 0.0f, 0.0f, 1.0f);
	M.mul(transf,M);
    }
    
	  /** Construct a perspective projection matrix for use in OpenGL
	   *
	   * The camera is located in the origin and look in the direction of the
	   * negative Z axis.
	   *
	   * Angle is the vertical view angle, in degrees. Aspect is the aspect ratio
	   * of the viewport, and near and far are the distances of the front and
	   * rear clipping plane from the camera.
	   *
	   * The perspective matrix (P) is right-multiplied with the current
	   * transformation matrix (M): M * P. Usually, you will want M to be
	   * the identity matrix when using this method.
	   */
	  public static void perspective(Matrix4f M, double angle, double aspect,
	      double nearPlane, double farPlane)
	  {
	    double halfHeight =
	      nearPlane * Math.tan(angle / 2 / 180 * 3.14159265358979323846);
	    double halfWidth = halfHeight * aspect;
	    frustum(M, -halfWidth, halfWidth, -halfHeight, halfHeight,
	      nearPlane, farPlane);
	  }

      public static GVector subtract(GVector v1, GVector v2)
      {
	  if (v1.getSize() != v2.getSize()) {
	    assert(v1.getSize() == 3 || v2.getSize() == 3);
	    assert(v1.getSize() == 4 || v2.getSize() == 4);
	    v1 = vec4ToVec3(v1);
	    v2 = vec4ToVec3(v2);
	  }
          GVector result = new GVector(v1.getSize());
          result.sub(v1, v2);
          return result;
      }

      public static GVector add(GVector v1, GVector v2)
      {
	  if (v1.getSize() != v2.getSize()) {
	    assert(v1.getSize() == 3 || v2.getSize() == 3);
	    assert(v1.getSize() == 4 || v2.getSize() == 4);
	    v1 = vec4ToVec3(v1);
	    v2 = vec4ToVec3(v2);
	  }
          GVector result = new GVector(v1.getSize());
          result.add(v1, v2);
          return result;
      }

      public static Matrix4f multiply(Matrix4f m1, Matrix4f m2)
      {
	Matrix4f result = new Matrix4f();
	result.mul(m1, m2);
	return result;
      }

      public static GVector multiply(GVector v, double scalar)
      {
          GVector result = new GVector(v.getSize());
          result.scale(scalar, v);
          return result;
      }

      public static GVector multiply(Matrix4f M, GVector v)
      {
          assert(v.getSize() == 4);
          GVector result = new GVector(4);
          GMatrix gM = new GMatrix(4, 4);
          gM.set(M);
          result.mul(gM, v);
          return result;
      }

      public static GVector normalize(GVector v)
      {
	  GVector result = new GVector(v.getSize());
	  result.normalize(v);
	  return result;
      }

      public static GVector cross(GVector v1, GVector v2)
      {
	assert(v1.getSize() == 3 && v2.getSize() == 3);
	GVector result = new GVector(3);
	result.setElement(0, v1.getElement(1) * v2.getElement(2) - v1.getElement(2) * v2.getElement(1));
	result.setElement(1, v1.getElement(2) * v2.getElement(0) - v1.getElement(0) * v2.getElement(2));
	result.setElement(2, v1.getElement(0) * v2.getElement(1) - v1.getElement(1) * v2.getElement(0));
	return result;
      }

      public static GVector vec4ToVec3(GVector v)
      {
	  if (v.getSize() == 4) {
	      GVector result = new GVector(3);
	      double w = v.getElement(3);
	      result.setElement(0, v.getElement(0) / w);
	      result.setElement(1, v.getElement(1) / w);
	      result.setElement(2, v.getElement(2) / w);
	      return result;
	  } else {
	      assert(v.getSize() == 3);
	      return v;
	  }
      }
}
