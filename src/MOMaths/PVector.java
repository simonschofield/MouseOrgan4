package MOMaths;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org
  Copyright (c) 2012-16 The Processing Foundation
  Copyright (c) 2008-12 Ben Fry and Casey Reas
  Copyright (c) 2008 Dan Shiffman
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License version 2.1 as published by the Free Software Foundation.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */


public class PVector implements Serializable{
	

	/**
	 * ( begin auto-generated from PVector_x.xml )
	 *
	 * The x component of the vector. This field (variable) can be used to both get
	 * and set the value (see above example.)
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:field
	 * @usage web_application
	 * @brief The x component of the vector
	 */
	public float x;

	/**
	 * ( begin auto-generated from PVector_y.xml )
	 *
	 * The y component of the vector. This field (variable) can be used to both get
	 * and set the value (see above example.)
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:field
	 * @usage web_application
	 * @brief The y component of the vector
	 */
	public float y;

	/**
	 * ( begin auto-generated from PVector_z.xml )
	 *
	 * The z component of the vector. This field (variable) can be used to both get
	 * and set the value (see above example.)
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:field
	 * @usage web_application
	 * @brief The z component of the vector
	 */
	public float z;

	/** Array so that this can be temporarily used in an array context */
	transient protected float[] array;

	/**
	 * Constructor for an empty vector: x, y, and z are set to 0.
	 */
	public PVector() {
	}

	/**
	 * Constructor for a 3D vector.
	 *
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @param z the z coordinate.
	 */
	public PVector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Constructor for a 2D vector: z coordinate is set to 0.
	 */
	public PVector(float x, float y) {
		this.x = x;
		this.y = y;
		this.z = 0;
	}

	/**
	 * ( begin auto-generated from PVector_set.xml )
	 *
	 * Sets the x, y, and z component of the vector using two or three separate
	 * variables, the data from a PVector, or the values from a float array.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @param x the x component of the vector
	 * @param y the y component of the vector
	 * @param z the z component of the vector
	 * @brief Set the components of the vector
	 */
	public PVector set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * @param x the x component of the vector
	 * @param y the y component of the vector
	 */
	public PVector set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/**
	 * @param v any variable of type PVector
	 */
	public PVector set(PVector v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}

	/**
	 * Set the x, y (and maybe z) coordinates using a float[] array as the source.
	 * 
	 * @param source array to copy from
	 */
	public PVector set(float[] source) {
		if (source.length >= 2) {
			x = source[0];
			y = source[1];
		}
		if (source.length >= 3) {
			z = source[2];
		}
		return this;
	}

	/**
	 * ( begin auto-generated from PVector_sub.xml )
	 *
	 * Make a new 2D unit vector from an angle.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Make a new 2D unit vector from an angle
	 * @param angle the angle in radians
	 * @return the new unit PVector
	 */
	static public PVector fromAngle(float angle) {
		return fromAngle(angle, null);
	}

	/**
	 * Make a new 2D unit vector from an angle
	 *
	 * @param target the target vector (if null, a new vector will be created)
	 * @return the PVector
	 */
	static public PVector fromAngle(float angle, PVector target) {
		if (target == null) {
			target = new PVector((float) Math.cos(angle), (float) Math.sin(angle), 0);
		} else {
			target.set((float) Math.cos(angle), (float) Math.sin(angle), 0);
		}
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_copy.xml )
	 *
	 * Gets a copy of the vector, returns a PVector object.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Get a copy of the vector
	 */
	public PVector copy() {
		return new PVector(x, y, z);
	}

	/**
	 * ( begin auto-generated from PVector_mag.xml )
	 *
	 * Calculates the magnitude (length) of the vector and returns the result as a
	 * float (this is simply the equation <em>sqrt(x*x + y*y + z*z)</em>.)
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Calculate the magnitude of the vector
	 * @return magnitude (length) of the vector
	 * @see PVector#magSq()
	 */
	public float mag() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * ( begin auto-generated from PVector_mag.xml )
	 *
	 * Calculates the squared magnitude of the vector and returns the result as a
	 * float (this is simply the equation <em>(x*x + y*y + z*z)</em>.) Faster if the
	 * real length is not required in the case of comparing vectors, etc.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Calculate the magnitude of the vector, squared
	 * @return squared magnitude of the vector
	 * @see PVector#mag()
	 */
	public float magSq() {
		return (x * x + y * y + z * z);
	}

	/**
	 * ( begin auto-generated from PVector_add.xml )
	 *
	 * Adds x, y, and z components to a vector, adds one vector to another, or adds
	 * two independent vectors together. The version of the method that adds two
	 * vectors together is a static method and returns a PVector, the others have no
	 * return value -- they act directly on the vector. See the examples for more
	 * context.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @param v the vector to be added
	 * @brief Adds x, y, and z components to a vector, one vector to another, or two
	 *        independent vectors
	 */
	public PVector add(PVector v) {
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	/**
	 * @param x x component of the vector
	 * @param y y component of the vector
	 */
	public PVector add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	/**
	 * @param z z component of the vector
	 */
	public PVector add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	/**
	 * Add two vectors
	 * 
	 * @param v1 a vector
	 * @param v2 another vector
	 */
	static public PVector add(PVector v1, PVector v2) {
		return add(v1, v2, null);
	}

	/**
	 * Add two vectors into a target vector
	 * 
	 * @param target the target vector (if null, a new vector will be created)
	 */
	static public PVector add(PVector v1, PVector v2, PVector target) {
		if (target == null) {
			target = new PVector(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
		} else {
			target.set(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
		}
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_sub.xml )
	 *
	 * Subtracts x, y, and z components from a vector, subtracts one vector from
	 * another, or subtracts two independent vectors. The version of the method that
	 * subtracts two vectors is a static method and returns a PVector, the others
	 * have no return value -- they act directly on the vector. See the examples for
	 * more context.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @param v any variable of type PVector
	 * @brief Subtract x, y, and z components from a vector, one vector from
	 *        another, or two independent vectors
	 */
	public PVector sub(PVector v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}

	/**
	 * @param x the x component of the vector
	 * @param y the y component of the vector
	 */
	public PVector sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	/**
	 * @param z the z component of the vector
	 */
	public PVector sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	/**
	 * Subtract one vector from another
	 * 
	 * @param v1 the x, y, and z components of a PVector object
	 * @param v2 the x, y, and z components of a PVector object
	 */
	static public PVector sub(PVector v1, PVector v2) {
		return sub(v1, v2, null);
	}

	/**
	 * Subtract one vector from another and store in another vector
	 * 
	 * @param target PVector in which to store the result
	 */
	static public PVector sub(PVector v1, PVector v2, PVector target) {
		if (target == null) {
			target = new PVector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
		} else {
			target.set(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
		}
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_mult.xml )
	 *
	 * Multiplies a vector by a scalar or multiplies one vector by another.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Multiply a vector by a scalar
	 * @param n the number to multiply with the vector
	 */
	public PVector mult(float n) {
		x *= n;
		y *= n;
		z *= n;
		return this;
	}

	/**
	 * @param v the vector to multiply by the scalar
	 */
	static public PVector mult(PVector v, float n) {
		return mult(v, n, null);
	}

	/**
	 * Multiply a vector by a scalar, and write the result into a target PVector.
	 * 
	 * @param target PVector in which to store the result
	 */
	static public PVector mult(PVector v, float n, PVector target) {
		if (target == null) {
			target = new PVector(v.x * n, v.y * n, v.z * n);
		} else {
			target.set(v.x * n, v.y * n, v.z * n);
		}
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_div.xml )
	 *
	 * Divides a vector by a scalar or divides one vector by another.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Divide a vector by a scalar
	 * @param n the number by which to divide the vector
	 */
	public PVector div(float n) {
		x /= n;
		y /= n;
		z /= n;
		return this;
	}

	/**
	 * Divide a vector by a scalar and return the result in a new vector.
	 * 
	 * @param v the vector to divide by the scalar
	 * @return a new vector that is v1 / n
	 */
	static public PVector div(PVector v, float n) {
		return div(v, n, null);
	}

	/**
	 * Divide a vector by a scalar and store the result in another vector.
	 * 
	 * @param target PVector in which to store the result
	 */
	static public PVector div(PVector v, float n, PVector target) {
		if (target == null) {
			target = new PVector(v.x / n, v.y / n, v.z / n);
		} else {
			target.set(v.x / n, v.y / n, v.z / n);
		}
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_dist.xml )
	 *
	 * Calculates the Euclidean distance between two points (considering a point as
	 * a vector object).
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @param v the x, y, and z coordinates of a PVector
	 * @brief Calculate the distance between two points
	 */
	public float dist(PVector v) {
		float dx = x - v.x;
		float dy = y - v.y;
		float dz = z - v.z;
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public float distSq(PVector v) {
		float dx = x - v.x;
		float dy = y - v.y;
		float dz = z - v.z;
		return (float) (dx * dx + dy * dy + dz * dz);
	}

	public float distXY(PVector v) {
		float dx = x - v.x;
		float dy = y - v.y;

		return (float) Math.sqrt(dx * dx + dy * dy);
	}
	
	public float distXZ(PVector v) {
		float dx = x - v.x;
		float dz = z - v.z;

		return (float) Math.sqrt(dx * dx + dz * dz);
	}

	/**
	 * @param v1 any variable of type PVector
	 * @param v2 any variable of type PVector
	 * @return the Euclidean distance between v1 and v2
	 */
	static public float dist(PVector v1, PVector v2) {
		float dx = v1.x - v2.x;
		float dy = v1.y - v2.y;
		float dz = v1.z - v2.z;
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * ( begin auto-generated from PVector_dot.xml )
	 *
	 * Calculates the dot product of two vectors.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @param v any variable of type PVector
	 * @return the dot product
	 * @brief Calculate the dot product of two vectors
	 */
	public float dot(PVector v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * @param x x component of the vector
	 * @param y y component of the vector
	 * @param z z component of the vector
	 */
	public float dot(float x, float y, float z) {
		return this.x * x + this.y * y + this.z * z;
	}

	/**
	 * @param v1 any variable of type PVector
	 * @param v2 any variable of type PVector
	 */
	static public float dot(PVector v1, PVector v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	/**
	 * ( begin auto-generated from PVector_cross.xml )
	 *
	 * Calculates and returns a vector composed of the cross product between two
	 * vectors.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @param v the vector to calculate the cross product
	 * @brief Calculate and return the cross product
	 */
	public PVector cross(PVector v) {
		return cross(v, null);
	}

	/**
	 * @param v      any variable of type PVector
	 * @param target PVector to store the result
	 */
	public PVector cross(PVector v, PVector target) {
		float crossX = y * v.z - v.y * z;
		float crossY = z * v.x - v.z * x;
		float crossZ = x * v.y - v.x * y;

		if (target == null) {
			target = new PVector(crossX, crossY, crossZ);
		} else {
			target.set(crossX, crossY, crossZ);
		}
		return target;
	}

	/**
	 * @param v1     any variable of type PVector
	 * @param v2     any variable of type PVector
	 * @param target PVector to store the result
	 */
	static public PVector cross(PVector v1, PVector v2, PVector target) {
		float crossX = v1.y * v2.z - v2.y * v1.z;
		float crossY = v1.z * v2.x - v2.z * v1.x;
		float crossZ = v1.x * v2.y - v2.x * v1.y;

		if (target == null) {
			target = new PVector(crossX, crossY, crossZ);
		} else {
			target.set(crossX, crossY, crossZ);
		}
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_normalize.xml )
	 *
	 * Normalize the vector to length 1 (make it a unit vector).
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Normalize the vector to a length of 1
	 */
	public PVector normalize() {
		float m = mag();
		if (m != 0 && m != 1) {
			div(m);
		}
		return this;
	}

	/**
	 * @param target Set to null to create a new vector
	 * @return a new vector (if target was null), or target
	 */
	public PVector normalize(PVector target) {
		if (target == null) {
			target = new PVector();
		}
		float m = mag();
		if (m > 0) {
			target.set(x / m, y / m, z / m);
		} else {
			target.set(x, y, z);
		}
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_limit.xml )
	 *
	 * Limit the magnitude of this vector to the value used for the <b>max</b>
	 * parameter.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @param max the maximum magnitude for the vector
	 * @brief Limit the magnitude of the vector
	 */
	public PVector limit(float max) {
		if (magSq() > max * max) {
			normalize();
			mult(max);
		}
		return this;
	}

	/**
	 * ( begin auto-generated from PVector_setMag.xml )
	 *
	 * Set the magnitude of this vector to the value used for the <b>len</b>
	 * parameter.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @param len the new length for this vector
	 * @brief Set the magnitude of the vector
	 */
	public PVector setMag(float len) {
		normalize();
		mult(len);
		return this;
	}

	/**
	 * Sets the magnitude of this vector, storing the result in another vector.
	 * 
	 * @param target Set to null to create a new vector
	 * @param len    the new length for the new vector
	 * @return a new vector (if target was null), or target
	 */
	public PVector setMag(PVector target, float len) {
		target = normalize(target);
		target.mult(len);
		return target;
	}

	/**
	 * ( begin auto-generated from PVector_setMag.xml )
	 *
	 * Calculate the angle of rotation for this vector (only 2D vectors)
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @return the angle of rotation
	 * @brief Calculate the angle of rotation for this vector
	 */
	public float heading() {
		float angle = (float) Math.atan2(y, x);
		return angle;
	}

	

	/**
	 * ( begin auto-generated from PVector_rotate.xml )
	 *
	 * Rotate the vector by an angle (only 2D vectors), magnitude remains the same
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Rotate the vector by an angle (2D only)
	 * @param theta the angle of rotation
	 */
	public PVector rotate(float theta) {
		float temp = x;
		// Might need to check for rounding errors like with angleBetween function?
		x = x * MOMaths.cos(theta) - y * MOMaths.sin(theta);
		y = temp * MOMaths.sin(theta) + y * MOMaths.cos(theta);
		return this;
	}

	/**
	 * ( begin auto-generated from PVector_rotate.xml )
	 *
	 * Linear interpolate the vector to another vector
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @brief Linear interpolate the vector to another vector
	 * @param v   the vector to lerp to
	 * @param amt The amount of interpolation; some value between 0.0 (old vector)
	 *            and 1.0 (new vector). 0.1 is very near the old vector; 0.5 is
	 *            halfway in between.
	 * @see PApplet#lerp(float, float, float)
	 */
	public PVector lerp(PVector v, float amt) {
		x = MOMaths.lerp(amt, x, v.x);
		y = MOMaths.lerp(amt, y, v.y);
		z = MOMaths.lerp(amt, z, v.z);
		return this;
	}

	/**
	 * Linear interpolate between two vectors (returns a new PVector object)
	 * 
	 * @param v1 the vector to start from
	 * @param v2 the vector to lerp to
	 */
	public static PVector lerp(PVector v1, PVector v2, float amt) {
		PVector v = v1.copy();
		v.lerp(v2, amt);
		return v;
	}

	/**
	 * Linear interpolate the vector to x,y,z values
	 * 
	 * @param x the x component to lerp to
	 * @param y the y component to lerp to
	 * @param z the z component to lerp to
	 */
	public PVector lerp(float x, float y, float z, float amt) {
		this.x = MOMaths.lerp(amt, this.x, x);
		this.y = MOMaths.lerp(amt, this.y, y);
		this.z = MOMaths.lerp(amt, this.z, z);
		return this;
	}
	
	
	float pNorm(PVector p, PVector startPt, PVector endPt){
	    // return the normalised value of the p as it journeys from startP, to endP, 
	    // based on distance between
	    // so returns 0 when p == startPt, 
	    // return 1 when p == endPt
	    // but can return a value <0 easily
	    float largestDist = startPt.dist(endPt);
	    float thisDist = p.dist(startPt);
	    return MOMaths.norm(thisDist, 0, largestDist);
	  }

	/**
	 * ( begin auto-generated from PVector_angleBetween.xml )
	 *
	 * Calculates and returns the angle (in radians) between two vectors.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage web_application
	 * @param v1 the x, y, and z components of a PVector
	 * @param v2 the x, y, and z components of a PVector
	 * @brief Calculate and return the angle between two vectors
	 */
	
	
	
	
	static public float angleBetween(PVector v1, PVector v2) {

		// We get NaN if we pass in a zero vector which can cause problems
		// Zero seems like a reasonable angle between a (0,0,0) vector and something
		// else
		if (v1.x == 0 && v1.y == 0 && v1.z == 0)
			return 0.0f;
		if (v2.x == 0 && v2.y == 0 && v2.z == 0)
			return 0.0f;

		double dot = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
		double v1mag = Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
		double v2mag = Math.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
		// This should be a number between -1 and 1, since it's "normalized"
		double amt = dot / (v1mag * v2mag);
		// But if it's not due to rounding error, then we need to fix it
		// http://code.google.com/p/processing/issues/detail?id=340
		// Otherwise if outside the range, acos() will return NaN
		// http://www.cppreference.com/wiki/c/math/acos
		if (amt <= -1) {
			return (float) Math.PI;
		} else if (amt >= 1) {
			// http://code.google.com/p/processing/issues/detail?id=435
			return 0;
		}
		return (float) Math.acos(amt);
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + ", " + z + " ]";
	}

	public void fromString(String s) {
		String[] sa = s.split(" ");
		x = Float.valueOf(sa[0]);
		y = Float.valueOf(sa[1]);
		z = Float.valueOf(sa[2]);

	}

	/**
	 * ( begin auto-generated from PVector_array.xml )
	 *
	 * Return a representation of this vector as a float array. This is only for
	 * temporary use. If used in any other fashion, the contents should be copied by
	 * using the <b>PVector.get()</b> method to copy into your own array.
	 *
	 * ( end auto-generated )
	 *
	 * @webref pvector:method
	 * @usage: web_application
	 * @brief Return a representation of the vector as a float array
	 */
	public float[] array() {
		if (array == null) {
			array = new float[3];
		}
		array[0] = x;
		array[1] = y;
		array[2] = z;
		return array;
	}

	public String toStr() {
		return toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PVector)) {
			return false;
		}
		final PVector p = (PVector) obj;
		return x == p.x && y == p.y && z == p.z;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 31 * result + Float.floatToIntBits(x);
		result = 31 * result + Float.floatToIntBits(y);
		result = 31 * result + Float.floatToIntBits(z);
		return result;
	}
	
	
	public static PVector ZERO() {
		return new PVector(0,0,0);
	}
	
	public static PVector UP() {
		return new PVector(0,1,0);
	}
	
	public PVector scale(float sx, float sy) {
		return scale(sx,sy,1);
	}
	
	public PVector scale(float sx, float sy, float sz) {
		return new PVector(x*sx,y*sy,z*sz);
	}
	
	public float getZ() { return z; }
	
	public static ArrayList<PVector> deepCopy(ArrayList<PVector> in) {
		ArrayList<PVector> deepCopy = new ArrayList<PVector>();
		for(PVector p: in) {
			deepCopy.add(p.copy());
		}
		return deepCopy;
	}
	
	public static PVector[] deepCopy(PVector[] in) {
		PVector[] deepCopy = new PVector[in.length];
		for(int n = 0; n < in.length; n++) {
			deepCopy[n] = in[n].copy();
		}
		return deepCopy;
	}
	
	
}
