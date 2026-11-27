{
  "version": "2.0.0",
  "timestamp": "2026-11-19T21:26:55.826Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "r-ru-open",
      "color": "#4e79a7",
      "name": "to RIGHT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 96.1
      }
    },
    {
      "id": "r-ru-wait-park",
      "color": "#4e79a7",
      "name": "RIGHT_AUTO_HIVE to RIGHT_PARK_WALL (guard)",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 16,
          "y": 62
        },
        {
          "x": 14,
          "y": 72
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 96.1,
        "endDeg": 270,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 96.1,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 96.10000000000002
      }
    },
    {
      "id": "r-ru-garden",
      "color": "#4e79a7",
      "name": "to GARDEN_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 96.1,
        "endDeg": 270
      }
    },
    {
      "id": "r-ru-garden-park",
      "color": "#4e79a7",
      "name": "GARDEN to RIGHT_PARK_WALL (guard)",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 30
        },
        {
          "x": 30,
          "y": 46
        },
        {
          "x": 16,
          "y": 62
        },
        {
          "x": 14,
          "y": 72
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 11.5,
        "y": 22.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "ruh2-garden-to-fix",
      "color": "#59a14f",
      "name": "GARDEN up the corridor under the HIVE to R_HIVE_EXIT",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60.5,
        "y": 100
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 12
        },
        {
          "x": 61,
          "y": 14
        },
        {
          "x": 61,
          "y": 40
        },
        {
          "x": 61,
          "y": 70
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "ruh2-exit-park",
      "color": "#4e79a7",
      "name": "R_HIVE_EXIT to RIGHT_PARK_WALL",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 50,
          "y": 108
        },
        {
          "x": 33,
          "y": 104
        },
        {
          "x": 33,
          "y": 86
        },
        {
          "x": 33,
          "y": 76
        },
        {
          "x": 33,
          "y": 72
        },
        {
          "x": 14.5,
          "y": 70
        },
        {
          "x": 14.5,
          "y": 80
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "ruh2-link-x",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60.5,
        "y": 100
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "ruh2-exit-to-fix",
      "color": "#4e79a7",
      "name": "R_HIVE_EXIT round to R_LEFT_FIX",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.5,
        "y": 114
      },
      "controlPoints": [
        {
          "x": 55,
          "y": 103
        },
        {
          "x": 60,
          "y": 106
        },
        {
          "x": 62,
          "y": 110
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 268.4
      }
    },
    {
      "id": "ruh2-fix-park-0",
      "color": "#4e79a7",
      "name": "LEFT not up: R_LEFT_FIX to RIGHT_PARK_WALL",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 121
        },
        {
          "x": 33,
          "y": 104
        },
        {
          "x": 33,
          "y": 86
        },
        {
          "x": 33,
          "y": 76
        },
        {
          "x": 33,
          "y": 72
        },
        {
          "x": 14.5,
          "y": 70
        },
        {
          "x": 14.5,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 268.4,
        "endDeg": 270,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 268.4,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "ruh2-link-a",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.5,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.4
      }
    },
    {
      "id": "ruh2-fix-to-sf",
      "color": "#f28e2b",
      "name": "R_LEFT_FIX to SCORING_FLOWER_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 268.4,
        "endDeg": 90
      }
    },
    {
      "id": "ruh2-sf-park",
      "color": "#f28e2b",
      "name": "SCORING_FLOWER_STAGING park",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 38,
          "y": 121
        },
        {
          "x": 32.5,
          "y": 106
        },
        {
          "x": 32.5,
          "y": 86
        },
        {
          "x": 32.5,
          "y": 76
        },
        {
          "x": 32.5,
          "y": 72
        },
        {
          "x": 14.5,
          "y": 70
        },
        {
          "x": 14.5,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 90,
        "endDeg": -90,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": -90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "ruh2-link-b",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 47.2,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "ruh2-sf-to-fix",
      "color": "#f28e2b",
      "name": "SCORING_FLOWER_STAGING to R_LEFT_FIX",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.5,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 268.4
      }
    },
    {
      "id": "r-ru-fix-park",
      "color": "#4e79a7",
      "name": "R_LEFT_FIX to RIGHT_PARK_WALL",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 121
        },
        {
          "x": 33,
          "y": 104
        },
        {
          "x": 33,
          "y": 86
        },
        {
          "x": 33,
          "y": 76
        },
        {
          "x": 33,
          "y": 72
        },
        {
          "x": 14.5,
          "y": 70
        },
        {
          "x": 14.5,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 268.4,
        "endDeg": 270,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 268.4,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "ruh2-link-c",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.5,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.4
      }
    },
    {
      "id": "r-ru-fix-skip-park",
      "color": "#4e79a7",
      "name": "LEFT down, R_LEFT_FIX to RIGHT_PARK_WALL",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 121
        },
        {
          "x": 33,
          "y": 104
        },
        {
          "x": 33,
          "y": 86
        },
        {
          "x": 33,
          "y": 76
        },
        {
          "x": 33,
          "y": 72
        },
        {
          "x": 14.5,
          "y": 70
        },
        {
          "x": 14.5,
          "y": 80
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 268.4,
        "endDeg": 270,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 268.4,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "ruh2-link-y",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60.5,
        "y": 100
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "ruh2-exit-to-rah",
      "color": "#59a14f",
      "name": "LEFT not up: R_HIVE_EXIT down the corridor to RIGHT_AUTO_HIVE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 70
        },
        {
          "x": 61,
          "y": 40
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 96.1
      }
    },
    {
      "id": "ruh2-rah-park",
      "color": "#4e79a7",
      "name": "RIGHT_AUTO_HIVE to RIGHT_PARK_WALL",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 16,
          "y": 62
        },
        {
          "x": 14,
          "y": 72
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 96.1,
        "endDeg": 270,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 96.1,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "ruh2-link-z",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 20.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 96.1
      }
    },
    {
      "id": "ruh2-rah-park-2",
      "color": "#4e79a7",
      "name": "RIGHT down too: RIGHT_AUTO_HIVE to RIGHT_PARK_WALL",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 92.5
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 16,
          "y": 62
        },
        {
          "x": 14,
          "y": 72
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 96.1,
        "endDeg": 270,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 96.1,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    }
  ],
  "shapes": [
    {
      "id": "biobuzz-red-hive",
      "name": "Red HIVE",
      "vertices": [
        {
          "x": 46,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 50
        },
        {
          "x": 48.6,
          "y": 91.4
        },
        {
          "x": 46,
          "y": 91.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-blue-hive",
      "name": "Blue HIVE",
      "vertices": [
        {
          "x": 96.5,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 91.4
        },
        {
          "x": 93.9,
          "y": 50
        },
        {
          "x": 96.5,
          "y": 50
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-top",
      "name": "Scoring-Side FLOWER",
      "vertices": [
        {
          "x": 43.4,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 141.5
        },
        {
          "x": 50.1,
          "y": 139.4
        },
        {
          "x": 46.75,
          "y": 137.7
        },
        {
          "x": 43.4,
          "y": 139.4
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "biobuzz-flower-right",
      "name": "Blue Alliance FLOWER",
      "vertices": [
        {
          "x": 141.5,
          "y": 98.1
        },
        {
          "x": 139.4,
          "y": 98.1
        },
        {
          "x": 137.7,
          "y": 94.75
        },
        {
          "x": 139.4,
          "y": 91.4
        },
        {
          "x": 141.5,
          "y": 91.4
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-bottom",
      "name": "Audience-Side FLOWER",
      "vertices": [
        {
          "x": 91.15,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 0
        },
        {
          "x": 97.8,
          "y": 2.1
        },
        {
          "x": 94.5,
          "y": 3.8
        },
        {
          "x": 91.15,
          "y": 2.1
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    },
    {
      "id": "biobuzz-flower-left",
      "name": "Red Alliance FLOWER",
      "vertices": [
        {
          "x": 0,
          "y": 50.35
        },
        {
          "x": 2.1,
          "y": 50.35
        },
        {
          "x": 3.8,
          "y": 47
        },
        {
          "x": 2.1,
          "y": 43.65
        },
        {
          "x": 0,
          "y": 43.65
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "r-ru-open"
    },
    {
      "kind": "path",
      "lineId": "r-ru-wait-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "r-ru-garden"
    },
    {
      "kind": "path",
      "lineId": "r-ru-garden-park"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "ruh2-garden-to-fix"
    },
    {
      "kind": "path",
      "lineId": "ruh2-exit-park"
    },
    {
      "kind": "path",
      "lineId": "ruh2-link-x"
    },
    {
      "kind": "path",
      "lineId": "ruh2-exit-to-fix"
    },
    {
      "kind": "path",
      "lineId": "ruh2-fix-park-0"
    },
    {
      "kind": "path",
      "lineId": "ruh2-link-a"
    },
    {
      "kind": "path",
      "lineId": "ruh2-fix-to-sf"
    },
    {
      "kind": "path",
      "lineId": "ruh2-sf-park"
    },
    {
      "kind": "path",
      "lineId": "ruh2-link-b"
    },
    {
      "kind": "path",
      "lineId": "ruh2-sf-to-fix"
    },
    {
      "kind": "path",
      "lineId": "r-ru-fix-park"
    },
    {
      "kind": "path",
      "lineId": "ruh2-link-c"
    },
    {
      "kind": "path",
      "lineId": "r-ru-fix-skip-park"
    },
    {
      "kind": "path",
      "lineId": "ruh2-link-y"
    },
    {
      "kind": "path",
      "lineId": "ruh2-exit-to-rah"
    },
    {
      "kind": "path",
      "lineId": "ruh2-rah-park"
    },
    {
      "kind": "path",
      "lineId": "ruh2-link-z"
    },
    {
      "kind": "path",
      "lineId": "ruh2-rah-park-2"
    }
  ],
  "fieldPoints": [],
  "robot": {
    "forwardVelocity": 75,
    "strafeVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rLength": 15.44,
    "rWidth": 16,
    "rForward": 0,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 50,
    "maxDeceleration": 50,
    "features": [
      {
        "name": "FLOWER poker",
        "length": 1.5,
        "width": 1.5,
        "forward": 8.5,
        "left": 0,
        "collides": true
      }
    ]
  },
  "activePaths": [],
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "registry": {
      "actions": [
        "LaunchAll",
        "PickFlower",
        "GardenGather",
        "LaunchOne",
        "SpinUp"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "CellUp",
        "Tip",
        "CameraBlind",
        "Always"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.5,
        "PickFlower": 4,
        "GardenGather": 3,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "RIGHT_AUTO_HIVE": [
        59.2,
        20.9,
        96.1
      ],
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "R_LEFT_FIX": [
        61.5,
        114,
        268.4
      ],
      "RIGHT_PARK_WALL": [
        14.5,
        92.5,
        270
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "R_HIVE_EXIT": [
        60.5,
        100,
        90
      ]
    },
    "pathEnds": {
      "r-ru-open": "RIGHT_AUTO_HIVE",
      "r-ru-wait-park": "RIGHT_PARK_WALL",
      "r-ru-garden": "GARDEN_STAGING",
      "r-ru-garden-park": "RIGHT_PARK_WALL",
      "ruh2-garden-to-fix": "R_HIVE_EXIT",
      "ruh2-fix-park-0": "RIGHT_PARK_WALL",
      "ruh2-fix-to-sf": "SCORING_FLOWER_STAGING",
      "ruh2-sf-park": "RIGHT_PARK_WALL",
      "ruh2-sf-to-fix": "R_LEFT_FIX",
      "r-ru-fix-park": "RIGHT_PARK_WALL",
      "r-ru-fix-skip-park": "RIGHT_PARK_WALL",
      "ruh2-exit-to-fix": "R_LEFT_FIX",
      "ruh2-exit-park": "RIGHT_PARK_WALL",
      "ruh2-exit-to-rah": "RIGHT_AUTO_HIVE",
      "ruh2-rah-park": "RIGHT_PARK_WALL",
      "ruh2-rah-park-2": "RIGHT_PARK_WALL"
    },
    "cards": [
      {
        "id": "c-r-spin",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-r-open",
        "kind": "path",
        "lineId": "r-ru-open",
        "park": false
      },
      {
        "id": "c-r-pre",
        "kind": "action",
        "name": "LaunchAll",
        "timeoutS": 1
      },
      {
        "id": "c-r-garden",
        "kind": "path",
        "lineId": "r-ru-garden",
        "park": false
      },
      {
        "id": "c-r-gather",
        "kind": "action",
        "name": "GardenGather",
        "timeoutS": 5.5
      },
      {
        "id": "c-ruh2-to-exit",
        "kind": "path",
        "lineId": "ruh2-garden-to-fix",
        "park": false
      },
      {
        "id": "c-ruh2-exit-check",
        "kind": "firstOf",
        "label": "LEFT up? (the partner has shot and gone)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "c-ruh2-to-fix",
                "kind": "path",
                "lineId": "ruh2-exit-to-fix",
                "park": false
              },
              {
                "id": "c-ruh2-left-up",
                "kind": "firstOf",
                "label": "LEFT up?",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-ruh2-fire1",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c-ruh2-to-sf",
                        "kind": "path",
                        "lineId": "ruh2-fix-to-sf",
                        "park": false
                      },
                      {
                        "id": "c-ruh2-pick",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 8
                      },
                      {
                        "id": "c-ruh2-back",
                        "kind": "path",
                        "lineId": "ruh2-sf-to-fix",
                        "park": false
                      },
                      {
                        "id": "c-ruh2-left-up2",
                        "kind": "firstOf",
                        "label": "LEFT still up?",
                        "rows": [
                          {
                            "when": [
                              "LeftCellUp"
                            ],
                            "cards": [
                              {
                                "id": "c-ruh2-fire2",
                                "kind": "action",
                                "name": "LaunchAll"
                              },
                              {
                                "id": "c-ruh2-park",
                                "kind": "path",
                                "lineId": "r-ru-fix-park",
                                "park": true
                              }
                            ]
                          },
                          {
                            "afterMs": 750,
                            "cards": [
                              {
                                "id": "c-ruh2-park-2",
                                "kind": "path",
                                "lineId": "r-ru-fix-skip-park",
                                "park": true
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "afterMs": 750,
                    "cards": [
                      {
                        "id": "c-ruh2-park-0",
                        "kind": "path",
                        "lineId": "ruh2-fix-park-0",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 750,
            "cards": [
              {
                "id": "c-ruh2-down",
                "kind": "path",
                "lineId": "ruh2-exit-to-rah",
                "park": false
              },
              {
                "id": "c-ruh2-right-up",
                "kind": "firstOf",
                "label": "RIGHT up? (our preloads missed)",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c-ruh2-fire-r",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c-ruh2-rah-park",
                        "kind": "path",
                        "lineId": "ruh2-rah-park",
                        "park": true
                      }
                    ]
                  },
                  {
                    "afterMs": 750,
                    "cards": [
                      {
                        "id": "c-ruh2-rah-park-2",
                        "kind": "path",
                        "lineId": "ruh2-rah-park-2",
                        "park": true
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "ruh2-link-a",
      "ruh2-link-b",
      "ruh2-link-c",
      "ruh2-link-x",
      "ruh2-link-y",
      "ruh2-link-z"
    ],
    "parkFrom": {
      "c-r-open": "r-ru-wait-park",
      "c-r-garden": "r-ru-garden-park",
      "c-ruh2-to-exit": "ruh2-exit-park",
      "c-ruh2-to-fix": "ruh2-fix-park-0",
      "c-ruh2-to-sf": "ruh2-sf-park",
      "c-ruh2-back": "r-ru-fix-park",
      "c-ruh2-down": "ruh2-rah-park"
    },
    "teamSpots": [
      "GARDEN_STAGING",
      "RIGHT_AUTO_HIVE",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING"
    ],
    "exportName": "R-RightUnderHive"
  }
}
