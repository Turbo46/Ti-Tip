package com.rpljumat.ti_tip

public class User(val nama: String, val username: String, val nik: String, val phone: String)
public class Goods(val userId: String, val agentId: String, val nama: String, val status: Int,
                   val ts: Long, val exp: Long, val estPrice: Int,
                   val length: Int, val width: Int, val height: Int, val weight: Int,
                   val fragile: Boolean, val grocery: Boolean)