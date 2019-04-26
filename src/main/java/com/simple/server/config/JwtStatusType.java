package com.simple.server.config;

public enum JwtStatusType {

		Authorized("Authorized"), Expired("Expired"), UnAuhorized("UnAuhorized"), InternalServerError("UnAuhorized"), RevokeToken("RevokeToken") ;
		
		private final String value;

		JwtStatusType(String value) {
			this.value = value;
		}

		public static JwtStatusType fromValue(String value) {
			if (value != null) {
				for (JwtStatusType e : values()) {
					if (e.value.equals(value)) {
						return e;
					}
				}
			}
			return getDefault();
		}

		public String toValue() {
			return value;
		}

		public static JwtStatusType getDefault() {
			return UnAuhorized;
		}
}
