package com.gerardbradshaw.fairday.activities.detail.utils

enum class CloudType {
  CLEAR {
    override val cloudCount = 0
    override val cloudTint = 0
  },
  FEW {
    override val cloudCount = 2
    override val cloudTint = 0
  },
  SCATTERED {
    override val cloudCount = 3
    override val cloudTint = 0
  },
  BROKEN {
    override val cloudCount = 4
    override val cloudTint = 0
  },
  SHOWER {
    override val cloudCount = 5
    override val cloudTint = 0
  },
  RAIN {
    override val cloudCount = 6
    override val cloudTint = 0
  },
  THUNDERSTORM {
    override val cloudCount = 8
    override val cloudTint = 0
  };

  abstract val cloudCount: Int
  abstract val cloudTint: Int
}