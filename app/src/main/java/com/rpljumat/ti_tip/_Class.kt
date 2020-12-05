package com.rpljumat.ti_tip

public const val AWAITING_CONFIRMATION = 1
public const val STORED = 2
public const val REJECTED = 3
public const val AWAITING_PINDAH_TITIP_ORG = 4
public const val AWAITING_PINDAH_TITIP_DEST = 5
public const val AWAITING_RETURN = 6
public const val RETURNED = 7
public const val EXPIRED = 8

public const val MS_SEHARI = 86_400_000

public class User(val nama: String, val username: String, val nik: String, val phone: String)
public class Goods(val userId: String, val agentId: String, val nama: String, val agentCnt: Int,
                   val status: Int, val ts: Long, val exp: Long, val estPrice: Int,
                   val length: Int, val width: Int, val height: Int, val weight: Int,
                   val fragile: Boolean, val grocery: Boolean)