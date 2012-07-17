package com.andredittrich.surface3d;

/*
 * Author: Sami Salkosuo, sami.salkosuo@fi.ibm.com
 *
 * (c) Copyright IBM Corp. 2007
 */

public class CoordinateConversion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		CoordinateConversion cc = new CoordinateConversion();
		String utm = cc.latLon2UTM(49, 8);
		System.out.println(utm);
		double[] gk = cc.latLon2GK(49, 8);
		System.out.println(gk);

	}

	public CoordinateConversion() {

	}

	public double[] latLon2GK(double latitude, double longitude) {
		LatLon2GK c = new LatLon2GK();
		return c.convertLatLonToGK(latitude, longitude);
	}

	public String latLon2UTM(double latitude, double longitude) {
		LatLon2UTM c = new LatLon2UTM();
		return c.convertLatLonToUTM(latitude, longitude);

	}

	private void validate(double latitude, double longitude) {
		if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0
				|| longitude >= 180.0) {
			throw new IllegalArgumentException(
					"Legal ranges: latitude [-90,90], longitude [-180,180).");
		}

	}

	public double degreeToRadian(double degree) {
		return degree * Math.PI / 180;
	}

	public double radianToDegree(double radian) {
		return radian * 180 / Math.PI;
	}

	private class LatLon2UTM {
		public String convertLatLonToUTM(double latitude, double longitude) {
			validate(latitude, longitude);
			String UTM = "";

			setVariables(latitude, longitude);

			String longZone = getLongZone(longitude);
			LatZones latZones = new LatZones();
			String latZone = latZones.getLatZone(latitude);

			double _easting = getEasting();
			double _northing = getNorthing(latitude);

			UTM = longZone + " " + latZone + " " + (_easting) + " "
					+ (_northing);			

			return UTM;

		}

		protected void setVariables(double latitude, double longitude) {
			latitude = degreeToRadian(latitude);
			rho = equatorialRadius
					* (1 - e * e)
					/ Math.pow(1 - Math.pow(e * Math.sin(latitude), 2), 3 / 2.0);

			nu = equatorialRadius
					/ Math.pow(1 - Math.pow(e * Math.sin(latitude), 2),
							(1 / 2.0));

			double var1;
			if (longitude < 0.0) {
				var1 = ((int) ((180 + longitude) / 6.0)) + 1;
			} else {
				var1 = ((int) (longitude / 6)) + 31;
			}
			double var2 = (6 * var1) - 183;
			double var3 = longitude - var2;
			p = var3 * 3600 / 10000;

			S = A0 * latitude - B0 * Math.sin(2 * latitude) + C0
					* Math.sin(4 * latitude) - D0 * Math.sin(6 * latitude) + E0
					* Math.sin(8 * latitude);

			K1 = S * k0;
			K2 = nu * Math.sin(latitude) * Math.cos(latitude)
					* Math.pow(sin1, 2) * k0 * (100000000) / 2;
			K3 = ((Math.pow(sin1, 4) * nu * Math.sin(latitude) * Math.pow(
					Math.cos(latitude), 3)) / 24)
					* (5 - Math.pow(Math.tan(latitude), 2) + 9 * e1sq
							* Math.pow(Math.cos(latitude), 2) + 4
							* Math.pow(e1sq, 2)
							* Math.pow(Math.cos(latitude), 4))
					* k0
					* (10000000000000000L);

			K4 = nu * Math.cos(latitude) * sin1 * k0 * 10000;

			K5 = Math.pow(sin1 * Math.cos(latitude), 3)
					* (nu / 6)
					* (1 - Math.pow(Math.tan(latitude), 2) + e1sq
							* Math.pow(Math.cos(latitude), 2)) * k0
					* 1000000000000L;

			A6 = (Math.pow(p * sin1, 6) * nu * Math.sin(latitude)
					* Math.pow(Math.cos(latitude), 5) / 720)
					* (61 - 58 * Math.pow(Math.tan(latitude), 2)
							+ Math.pow(Math.tan(latitude), 4) + 270 * e1sq
							* Math.pow(Math.cos(latitude), 2) - 330 * e1sq
							* Math.pow(Math.sin(latitude), 2)) * k0 * (1E+24);

		}

		protected String getLongZone(double longitude) {
			double longZone = 0;
			if (longitude < 0.0) {
				longZone = ((180.0 + longitude) / 6) + 1;
			} else {
				longZone = (longitude / 6) + 31;
			}
			String val = String.valueOf((int) longZone);
			if (val.length() == 1) {
				val = "0" + val;
			}
			return val;
		}

		protected double getNorthing(double latitude) {
			double northing = K1 + K2 * p * p + K3 * Math.pow(p, 4);
			if (latitude < 0.0) {
				northing = 10000000 + northing;
			}
			return northing;
		}

		protected double getEasting() {
			return 500000 + (K4 * p + K5 * Math.pow(p, 3));
		}

		// Lat Lon to UTM variables

		// equatorial radius
		double equatorialRadius = 6378137;

		// polar radius
		double polarRadius = 6356752.314;
		// Mean radius
		double rm = Math.pow(equatorialRadius * polarRadius, 1 / 2.0);
		// scale factor
		double k0 = 0.9996;
		// eccentricity
		double e = Math.sqrt(1 - Math.pow(polarRadius / equatorialRadius, 2));
		double e1sq = e * e / (1 - e * e);

		// r curv 1
		double rho;

		// r curv 2
		double nu;

		// Calculate Meridional Arc Length
		// Meridional Arc
		double S;

		double A0 = 6367449.146;

		double B0 = 16038.42955;

		double C0 = 16.83261333;

		double D0 = 0.021984404;

		double E0 = 0.000312705;

		// Calculation Constants
		// Delta Long
		double p = -0.483084;

		double sin1 = 4.84814E-06;

		// Coefficients for UTM Coordinates
		double K1;

		double K2;

		double K3;

		double K4;

		double K5;

		double A6;

	}

	private class LatLon2GK {
		public double[] convertLatLonToGK(double latitude, double longitude) {
			validate(latitude, longitude);
			double[] GK = new double[2];

			setVariables(latitude, longitude);

			String longZone = getLongZone(longitude);
			String GKlongZone = utm2gkZone(longZone);
			LatZones latZones = new LatZones();
			latZones.getLatZone(latitude);

			GK[0] = Double.parseDouble(GKlongZone + Double.toString(getEasting()));
			GK[1] = getNorthing(latitude);

//			UTM = GKlongZone + (_easting) + " " + (_northing);

			return GK;

		}

		private String utm2gkZone(String longZone) {

			double utmzone = Integer.parseInt(longZone);
			double gkzone = ((((utmzone - 30) * 6) - 3) / 3);

			String val = String.valueOf((int) gkzone);

			return val;
		}

		protected void setVariables(double latitude, double longitude) {
			latitude = degreeToRadian(latitude);
			rho = equatorialRadius
					* (1 - e * e)
					/ Math.pow(1 - Math.pow(e * Math.sin(latitude), 2), 3 / 2.0);
			nu = equatorialRadius
					/ Math.pow(1 - Math.pow(e * Math.sin(latitude), 2),
							(1 / 2.0));
			double var1;
			if (longitude < 0.0) {
				var1 = ((int) ((180 + longitude) / 6.0)) + 1;
			} else {
				var1 = ((int) (longitude / 6)) + 31;
			}
			double var2 = (6 * var1) - 183;
			double var3 = longitude - var2;
			p = var3 * 3600 / 10000;
			S = E0 * radianToDegree(latitude) + E2 * Math.sin(2 * latitude)
					+ E4 * Math.sin(4 * latitude) + E6 * Math.sin(6 * latitude);
			K1 = S * k0;
			K2 = nu * Math.sin(latitude) * Math.cos(latitude)
					* Math.pow(sin1, 2) * k0 * (100000000) / 2;
			K3 = ((Math.pow(sin1, 4) * nu * Math.sin(latitude) * Math.pow(
					Math.cos(latitude), 3)) / 24)
					* (5 - Math.pow(Math.tan(latitude), 2) + 9 * e1sq
							* Math.pow(Math.cos(latitude), 2) + 4
							* Math.pow(e1sq, 2)
							* Math.pow(Math.cos(latitude), 4))
					* k0
					* (10000000000000000L);
			K4 = nu * Math.cos(latitude) * sin1 * k0 * 10000;
			K5 = Math.pow(sin1 * Math.cos(latitude), 3)
					* (nu / 6)
					* (1 - Math.pow(Math.tan(latitude), 2) + e1sq
							* Math.pow(Math.cos(latitude), 2)) * k0
					* 1000000000000L;
			A6 = (Math.pow(p * sin1, 6) * nu * Math.sin(latitude)
					* Math.pow(Math.cos(latitude), 5) / 720)
					* (61 - 58 * Math.pow(Math.tan(latitude), 2)
							+ Math.pow(Math.tan(latitude), 4) + 270 * e1sq
							* Math.pow(Math.cos(latitude), 2) - 330 * e1sq
							* Math.pow(Math.sin(latitude), 2)) * k0 * (1E+24);
		}

		protected String getLongZone(double longitude) {
			double longZone = 0;
			if (longitude < 0.0) {
				longZone = ((180.0 + longitude) / 6) + 1;
			} else {
				longZone = (longitude / 6) + 31;
			}
			String val = String.valueOf((int) longZone);
			if (val.length() == 1) {
				val = "0" + val;
			}
			return val;
		}

		protected double getNorthing(double latitude) {
			double northing = K1 + K2 * p * p + K3 * Math.pow(p, 4);

			if (latitude < 0.0) {
				northing = 10000000 + northing;
			}
			return northing;
		}

		protected double getEasting() {
			double easting = 500000 + (K4 * p + K5 * Math.pow(p, 3));
			return easting;
		}

		// Lat Lon to UTM variables

		// equatorial radius
		double equatorialRadius = 6377397.155;

		// polar radius
		double polarRadius = 6356078.962818188;

		double n = (equatorialRadius - polarRadius)
				/ (equatorialRadius + polarRadius);

		// Mean radius
		double rm = Math.pow(equatorialRadius * polarRadius, 1 / 2.0);
		// scale factor
		double k0 = 1.0;
		// eccentricity
		double e = Math.sqrt(1 - Math.pow(polarRadius / equatorialRadius, 2));
		double e1sq = e * e / (1 - e * e);

		// r curv 1
		double rho;// = 6368573.744;

		// r curv 2
		double nu;// = 6389236.914;

		// Calculate Meridional Arc Length
		// Meridional Arc
		double S;// = 5103266.421;

		double E0 = 111120.619608;
		double E2 = -15988.6383;
		double E4 = 16.73;
		double E6 = -0.0218;

		// Calculation Constants
		// Delta Long
		double p;// = -0.483084;

		double sin1 = 4.84814E-06;

		// Coefficients for UTM Coordinates
		double K1;// = 5101225.115;

		double K2;// = 3750.291596;

		double K3;// = 1.397608151;

		double K4;// = 214839.3105;

		double K5;// = -2.995382942;

		double A6;// = -1.00541E-07;

	}

	private class LatZones {
		private char[] letters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
				'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

		private int[] degrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24,
				-16, -8, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
				'K', 'L', 'M' };

		private int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32,
				-24, -16, -8 };

		private char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
				'W', 'X', 'Z' };

		private int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private int arrayLength = 22;

		public LatZones() {
		}

		public int getLatZoneDegree(String letter) {
			char ltr = letter.charAt(0);
			for (int i = 0; i < arrayLength; i++) {
				if (letters[i] == ltr) {
					return degrees[i];
				}
			}
			return -100;
		}

		public String getLatZone(double latitude) {
			int latIndex = -2;
			int lat = (int) latitude;

			if (lat >= 0) {
				int len = posLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == posDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat > posDegrees[i]) {
						continue;
					} else {
						latIndex = i - 1;
						break;
					}
				}
			} else {
				int len = negLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == negDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat < negDegrees[i]) {
						latIndex = i - 1;
						break;
					} else {
						continue;
					}

				}

			}

			if (latIndex == -1) {
				latIndex = 0;
			}
			if (lat >= 0) {
				if (latIndex == -2) {
					latIndex = posLetters.length - 1;
				}
				return String.valueOf(posLetters[latIndex]);
			} else {
				if (latIndex == -2) {
					latIndex = negLetters.length - 1;
				}
				return String.valueOf(negLetters[latIndex]);

			}
		}

	}

}
