export interface Credentials {
  email: string;
  password: string;
}
export interface Account {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  activated: boolean;
}
export type Role = "owner" | "guest";
export interface Registration extends Credentials {
  accountType: "GUEST" | "OWNER";
  firstName: string;
  lastName: string;
  gender: string;
  contactNum: string;
  nationality?: string;
  dob?: string;
  preferences?: string[];
}
export interface Address {
  street: string;
  suite: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}
export interface PropertyInput {
  propertyName: string;
  description: string;
  address: Address;
  latitude: number;
  longitude: number;
  houseSize: number;
  landSize: number;
  baseRate: number;
}
export interface Property extends PropertyInput {
  propertyId: string;
}
export interface Dates {
  arrivalDate: string;
  departureDate: string;
}
export interface Booking extends Dates {
  bookingId: string;
  propertyId: string;
  baseCharge: number;
  gct: number;
  tef: number;
  gart: number;
  totalCharge: number;
  status: "CONFIRMED" | "CHECKED_IN" | "CHECKED_OUT" | "CANCELLED" | "NO_SHOW";
  paymentStatus: "PENDING" | "PAID" | "FAILED" | "REFUNDED";
  createdAt: string;
  updatedAt: string;
}
