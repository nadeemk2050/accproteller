package com.accpro.teller.data

data class LoginRequest(
    val action: String = "login",
    val apiKey: String,
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val userId: String?,
    val userName: String?,
    val companyId: String?,
    val companyName: String?
)

data class VoucherRequest(
    val action: String = "create_voucher",
    val type: String,        // "payment", "receipt", "contra"
    val date: String,
    val amount: Double,
    val drAccountId: String?,
    val crAccountId: String?,
    val drName: String?,
    val crName: String?,
    val narration: String?,
    val refNo: String?
)

data class VoucherResponse(
    val success: Boolean,
    val message: String?,
    val voucherId: String?,
    val refNo: String?
)

data class BalanceItem(
    val accountId: String,
    val accountName: String,
    val accountType: String,     // "cash" or "bank"
    val balance: Double
)

data class BalanceResponse(
    val success: Boolean,
    val message: String?,
    val balances: List<BalanceItem>?
)

data class AccountsResponse(
    val success: Boolean,
    val message: String?,
    val accounts: List<AccountItem>?
)

data class AccountItem(
    val id: String,
    val name: String,
    val type: String?     // "cash", "bank", etc.
)

data class ValidateKeyRequest(
    val action: String = "validate_key",
    val apiKey: String
)

data class TeamMember(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class ValidateKeyResponse(
    val success: Boolean,
    val companyName: String?,
    val companyId: String?,
    val team: List<TeamMember>?,
    val teamCount: Int?,
    val message: String?
)
