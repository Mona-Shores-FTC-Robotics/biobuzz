{
  "version": "2.0.0",
  "timestamp": "2026-11-28T01:55:30.000Z",
  "startPoint": {
    "x": 62.3,
    "y": 133.8,
    "locked": false,
    "headingDeg": 270
  },
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
  "lines": [
    {
      "id": "start-loff",
      "color": "#f28e2b",
      "name": "LEFT_START straight to LEFT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [
        {
          "x": 56,
          "y": 124
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 298.7
      }
    },
    {
      "id": "loff-park-7",
      "color": "#59a14f",
      "name": "guard after start-loff: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-1",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.7
      }
    },
    {
      "id": "loff-roff",
      "color": "#4e79a7",
      "name": "LEFT_AUTO_OFFSET down the outer lane to RIGHT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 105
        },
        {
          "x": 30,
          "y": 80
        },
        {
          "x": 28,
          "y": 50
        },
        {
          "x": 27,
          "y": 30
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 61
      }
    },
    {
      "id": "roff-park-3",
      "color": "#f28e2b",
      "name": "guard after loff-roff: RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        },
        {
          "x": 22,
          "y": 116
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "roff-wf",
      "color": "#59a14f",
      "name": "RIGHT_AUTO_OFFSET to WALL_FLOWER_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 30
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 61,
        "endDeg": 180
      }
    },
    {
      "id": "wf-park-2",
      "color": "#e15759",
      "name": "guard after roff-wf: WALL_FLOWER_STAGING to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 75
        },
        {
          "x": 30,
          "y": 105
        },
        {
          "x": 22,
          "y": 115
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "wf-loff",
      "color": "#76b7b2",
      "name": "WALL_FLOWER_STAGING up the outer lane to LEFT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 62
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 180,
        "endDeg": 298.7,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 270,
                "endDeg": 298.7
              }
            }
          ]
        }
      }
    },
    {
      "id": "loff-park-4",
      "color": "#edc948",
      "name": "guard after wf-loff: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.7
      }
    },
    {
      "id": "loff-park-6",
      "color": "#ff9da7",
      "name": "Fix failed: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-5",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.7
      }
    },
    {
      "id": "loff-park-5",
      "color": "#b07aa1",
      "name": "After the fix: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-6",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "roff-park-5",
      "color": "#4e79a7",
      "name": "Late TIP 1, no TIP 2: RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        },
        {
          "x": 22,
          "y": 116
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-7",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "roff-park-4",
      "color": "#9c755f",
      "name": "Late TIP 1: RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        },
        {
          "x": 22,
          "y": 116
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-8",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.7
      }
    },
    {
      "id": "loff-wf",
      "color": "#76b7b2",
      "name": "LEFT_AUTO_OFFSET down the outer lane to WALL_FLOWER_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 105
        },
        {
          "x": 30,
          "y": 80
        },
        {
          "x": 27,
          "y": 62
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 180
      }
    },
    {
      "id": "wf-park",
      "color": "#edc948",
      "name": "guard after loff-wf: WALL_FLOWER_STAGING to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 75
        },
        {
          "x": 30,
          "y": 105
        },
        {
          "x": 22,
          "y": 115
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "link-9",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "wf-roff",
      "color": "#b07aa1",
      "name": "WALL_FLOWER_STAGING to RIGHT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 30
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 61
      }
    },
    {
      "id": "roff-park",
      "color": "#ff9da7",
      "name": "guard after wf-roff: RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        },
        {
          "x": 22,
          "y": 116
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-10",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "roff-loff",
      "color": "#4e79a7",
      "name": "RIGHT_AUTO_OFFSET up the outer lane to LEFT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 30
        },
        {
          "x": 33,
          "y": 50
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": -61.3,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.25,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.25,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 0,
                "endDeg": -61.3
              }
            }
          ]
        }
      }
    },
    {
      "id": "loff-park",
      "color": "#f28e2b",
      "name": "guard after roff-loff: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-11",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -61.3
      }
    },
    {
      "id": "loff-park-3",
      "color": "#e15759",
      "name": "LEFT down: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-12",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -61.3
      }
    },
    {
      "id": "loff-park-2",
      "color": "#59a14f",
      "name": "LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-13",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "roff-park-2",
      "color": "#9c755f",
      "name": "RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        },
        {
          "x": 22,
          "y": 116
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-14",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 298.7
      }
    },
    {
      "id": "loff-turn",
      "color": "#59a14f",
      "name": "LEFT_AUTO_OFFSET: turn the intake to the LOADING ZONE",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40.2,
        "y": 120.8
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 215.0
      }
    },
    {
      "id": "turn-park",
      "color": "#f28e2b",
      "name": "guard after loff-turn: L_NECTAR_TURN to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 215.0,
        "endDeg": 0
      }
    },
    {
      "id": "link-15",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.2,
        "y": 120.8
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 215.0
      }
    },
    {
      "id": "turn-wf",
      "color": "#76b7b2",
      "name": "L_NECTAR_TURN down the outer lane to WALL_FLOWER_STAGING",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [
        {
          "x": 31,
          "y": 105
        },
        {
          "x": 30,
          "y": 80
        },
        {
          "x": 27,
          "y": 62
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 215.0,
        "endDeg": 180
      }
    },
    {
      "id": "wf-park-3",
      "color": "#edc948",
      "name": "guard after turn-wf: WALL_FLOWER_STAGING to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 75
        },
        {
          "x": 30,
          "y": 105
        },
        {
          "x": 22,
          "y": 115
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 0
      }
    },
    {
      "id": "link-16",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 23.1,
        "y": 47.2
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "wf-roff-2",
      "color": "#b07aa1",
      "name": "NECTAR: WALL_FLOWER_STAGING to RIGHT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 30
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 61
      }
    },
    {
      "id": "roff-park-6",
      "color": "#ff9da7",
      "name": "guard after wf-roff-2: RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        },
        {
          "x": 22,
          "y": 116
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    },
    {
      "id": "link-17",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "roff-loff-2",
      "color": "#4e79a7",
      "name": "NECTAR: RIGHT_AUTO_OFFSET up the outer lane to LEFT_AUTO_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 30
        },
        {
          "x": 33,
          "y": 50
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": -61.3,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.25,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.25,
              "endProgress": 0.8,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 0,
                "endDeg": -61.3
              }
            }
          ]
        }
      }
    },
    {
      "id": "loff-park-8",
      "color": "#f28e2b",
      "name": "guard after roff-loff-2: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-18",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -61.3
      }
    },
    {
      "id": "loff-park-9",
      "color": "#e15759",
      "name": "NECTAR, LEFT down: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-19",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 40.7,
        "y": 121.1
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": -61.3
      }
    },
    {
      "id": "loff-park-10",
      "color": "#59a14f",
      "name": "NECTAR: LEFT_AUTO_OFFSET to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 121
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 298.7,
        "endDeg": 0
      }
    },
    {
      "id": "link-20",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 32.1,
        "y": 16.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 61
      }
    },
    {
      "id": "roff-park-7",
      "color": "#9c755f",
      "name": "NECTAR: RIGHT_AUTO_OFFSET up the outer lane to LEFT_PARK",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.5,
        "y": 117.4
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 40
        },
        {
          "x": 33,
          "y": 80
        },
        {
          "x": 33,
          "y": 105
        },
        {
          "x": 22,
          "y": 116
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 61,
        "endDeg": 0,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 61,
                "endDeg": 0
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 0
              }
            }
          ]
        }
      }
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "start-loff"
    },
    {
      "kind": "path",
      "lineId": "loff-park-7"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "loff-roff"
    },
    {
      "kind": "path",
      "lineId": "roff-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "roff-wf"
    },
    {
      "kind": "path",
      "lineId": "wf-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "wf-loff"
    },
    {
      "kind": "path",
      "lineId": "loff-park-4"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "loff-park-6"
    },
    {
      "kind": "path",
      "lineId": "link-5"
    },
    {
      "kind": "path",
      "lineId": "loff-park-5"
    },
    {
      "kind": "path",
      "lineId": "link-6"
    },
    {
      "kind": "path",
      "lineId": "roff-park-5"
    },
    {
      "kind": "path",
      "lineId": "link-7"
    },
    {
      "kind": "path",
      "lineId": "roff-park-4"
    },
    {
      "kind": "path",
      "lineId": "link-8"
    },
    {
      "kind": "path",
      "lineId": "loff-wf"
    },
    {
      "kind": "path",
      "lineId": "wf-park"
    },
    {
      "kind": "path",
      "lineId": "link-9"
    },
    {
      "kind": "path",
      "lineId": "wf-roff"
    },
    {
      "kind": "path",
      "lineId": "roff-park"
    },
    {
      "kind": "path",
      "lineId": "link-10"
    },
    {
      "kind": "path",
      "lineId": "roff-loff"
    },
    {
      "kind": "path",
      "lineId": "loff-park"
    },
    {
      "kind": "path",
      "lineId": "link-11"
    },
    {
      "kind": "path",
      "lineId": "loff-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-12"
    },
    {
      "kind": "path",
      "lineId": "loff-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-13"
    },
    {
      "kind": "path",
      "lineId": "roff-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-14"
    },
    {
      "kind": "path",
      "lineId": "loff-turn"
    },
    {
      "kind": "path",
      "lineId": "turn-park"
    },
    {
      "kind": "path",
      "lineId": "link-15"
    },
    {
      "kind": "path",
      "lineId": "turn-wf"
    },
    {
      "kind": "path",
      "lineId": "wf-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-16"
    },
    {
      "kind": "path",
      "lineId": "wf-roff-2"
    },
    {
      "kind": "path",
      "lineId": "roff-park-6"
    },
    {
      "kind": "path",
      "lineId": "link-17"
    },
    {
      "kind": "path",
      "lineId": "roff-loff-2"
    },
    {
      "kind": "path",
      "lineId": "loff-park-8"
    },
    {
      "kind": "path",
      "lineId": "link-18"
    },
    {
      "kind": "path",
      "lineId": "loff-park-9"
    },
    {
      "kind": "path",
      "lineId": "link-19"
    },
    {
      "kind": "path",
      "lineId": "loff-park-10"
    },
    {
      "kind": "path",
      "lineId": "link-20"
    },
    {
      "kind": "path",
      "lineId": "roff-park-7"
    }
  ],
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "registry": {
      "actions": [
        "LaunchAll",
        "PickFlower",
        "GardenGather",
        "LaunchOne",
        "SpinUp",
        "LaunchOneAimed",
        "CatchNectar"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "CellUp",
        "Tip",
        "CameraBlind",
        "Always",
        "NectarIn"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.5,
        "PickFlower": 4,
        "GardenGather": 3,
        "LaunchOne": 0.5,
        "SpinUp": 0.1,
        "LaunchOneAimed": 1.2,
        "CatchNectar": 0.6
      }
    },
    "points": {
      "LEFT_AUTO_OFFSET": [
        40.7,
        121.1,
        298.7
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ],
      "LEFT_START": [
        62.3,
        133.8,
        270
      ],
      "RIGHT_AUTO_OFFSET": [
        32.1,
        16.4,
        61
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "L_NECTAR_TURN": [
        40.2,
        120.8,
        215.0
      ]
    },
    "pathEnds": {
      "roff-loff": "LEFT_AUTO_OFFSET",
      "loff-park": "LEFT_PARK",
      "loff-park-2": "LEFT_PARK",
      "loff-park-3": "LEFT_PARK",
      "loff-wf": "WALL_FLOWER_STAGING",
      "wf-park": "LEFT_PARK",
      "wf-roff": "RIGHT_AUTO_OFFSET",
      "roff-park": "LEFT_PARK",
      "roff-park-2": "LEFT_PARK",
      "loff-roff": "RIGHT_AUTO_OFFSET",
      "roff-park-3": "LEFT_PARK",
      "roff-wf": "WALL_FLOWER_STAGING",
      "wf-park-2": "LEFT_PARK",
      "wf-loff": "LEFT_AUTO_OFFSET",
      "loff-park-4": "LEFT_PARK",
      "loff-park-5": "LEFT_PARK",
      "loff-park-6": "LEFT_PARK",
      "roff-park-4": "LEFT_PARK",
      "roff-park-5": "LEFT_PARK",
      "start-loff": "LEFT_AUTO_OFFSET",
      "loff-park-7": "LEFT_PARK",
      "loff-turn": "L_NECTAR_TURN",
      "turn-park": "LEFT_PARK",
      "turn-wf": "WALL_FLOWER_STAGING",
      "wf-park-3": "LEFT_PARK",
      "wf-roff-2": "RIGHT_AUTO_OFFSET",
      "roff-park-6": "LEFT_PARK",
      "roff-loff-2": "LEFT_AUTO_OFFSET",
      "loff-park-8": "LEFT_PARK",
      "loff-park-9": "LEFT_PARK",
      "loff-park-10": "LEFT_PARK",
      "roff-park-7": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c27-spinup",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c28-start-loff",
        "kind": "path",
        "lineId": "start-loff",
        "park": false
      },
      {
        "id": "c29-check",
        "kind": "firstOf",
        "label": "LEFT up? (TIP 1 came; else cross and fix it)",
        "rows": [
          {
            "afterMs": 9500,
            "cards": [
              {
                "id": "c13-loff-roff",
                "kind": "path",
                "lineId": "loff-roff",
                "park": false
              },
              {
                "id": "c26-check",
                "kind": "firstOf",
                "label": "RIGHT still up? (TIP 1 never came: fix it)",
                "rows": [
                  {
                    "when": [
                      "CellUp"
                    ],
                    "cards": [
                      {
                        "id": "c14-launchall",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c15-roff-wf",
                        "kind": "path",
                        "lineId": "roff-wf",
                        "park": false
                      },
                      {
                        "id": "c16-pickflower",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 8
                      },
                      {
                        "id": "c17-wf-loff",
                        "kind": "path",
                        "lineId": "wf-loff",
                        "park": false
                      },
                      {
                        "id": "c21-check",
                        "kind": "firstOf",
                        "label": "LEFT up? (the fix tipped it: add the WALL FLOWER load)",
                        "rows": [
                          {
                            "afterMs": 25600,
                            "cards": [
                              {
                                "id": "c20-loff-park",
                                "kind": "path",
                                "lineId": "loff-park-6",
                                "park": true
                              }
                            ],
                            "matchClock": true
                          },
                          {
                            "when": [
                              "CellUp"
                            ],
                            "cards": [
                              {
                                "id": "c18-launchall",
                                "kind": "action",
                                "name": "LaunchAll"
                              },
                              {
                                "id": "c19-loff-park",
                                "kind": "path",
                                "lineId": "loff-park-5",
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
                        "id": "c25-check",
                        "kind": "firstOf",
                        "label": "RIGHT up? (TIP 1 came late; now TIP 2)",
                        "rows": [
                          {
                            "afterMs": 24600,
                            "cards": [
                              {
                                "id": "c24-roff-park",
                                "kind": "path",
                                "lineId": "roff-park-5",
                                "park": true
                              }
                            ],
                            "matchClock": true
                          },
                          {
                            "when": [
                              "CellUp"
                            ],
                            "cards": [
                              {
                                "id": "c22-launchall",
                                "kind": "action",
                                "name": "LaunchAll"
                              },
                              {
                                "id": "c23-roff-park",
                                "kind": "path",
                                "lineId": "roff-park-4",
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
            "matchClock": true
          },
          {
            "when": [
              "CellUp"
            ],
            "cards": [
              {
                "id": "c6-launchall",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "c43-check",
                "kind": "firstOf",
                "label": "Time for the human player's NECTAR? (a late TIP 1 skips it)",
                "rows": [
                  {
                    "afterMs": 11000,
                    "matchClock": true,
                    "cards": [
                      {
                        "id": "c7-loff-wf",
                        "kind": "path",
                        "lineId": "loff-wf",
                        "park": false
                      },
                      {
                        "id": "c8-pickflower",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 8
                      },
                      {
                        "id": "c9-wf-roff",
                        "kind": "path",
                        "lineId": "wf-roff",
                        "park": false
                      },
                      {
                        "id": "c12-check",
                        "kind": "firstOf",
                        "label": "RIGHT up? (TIP 2 came; else take this load to LEFT)",
                        "rows": [
                          {
                            "afterMs": 21500,
                            "cards": [
                              {
                                "id": "c1-roff-loff",
                                "kind": "path",
                                "lineId": "roff-loff",
                                "park": false
                              },
                              {
                                "id": "c5-check",
                                "kind": "firstOf",
                                "label": "LEFT still up? (TIP 2 is short: add this load)",
                                "rows": [
                                  {
                                    "afterMs": 25600,
                                    "cards": [
                                      {
                                        "id": "c4-loff-park",
                                        "kind": "path",
                                        "lineId": "loff-park-3",
                                        "park": true
                                      }
                                    ],
                                    "matchClock": true
                                  },
                                  {
                                    "when": [
                                      "CellUp"
                                    ],
                                    "cards": [
                                      {
                                        "id": "c2-launchall",
                                        "kind": "action",
                                        "name": "LaunchAll"
                                      },
                                      {
                                        "id": "c3-loff-park",
                                        "kind": "path",
                                        "lineId": "loff-park-2",
                                        "park": true
                                      }
                                    ]
                                  }
                                ]
                              }
                            ],
                            "matchClock": true
                          },
                          {
                            "when": [
                              "CellUp"
                            ],
                            "cards": [
                              {
                                "id": "c10-launchall",
                                "kind": "action",
                                "name": "LaunchAll"
                              },
                              {
                                "id": "c11-roff-park",
                                "kind": "path",
                                "lineId": "roff-park-2",
                                "park": true
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "when": [
                      "Always"
                    ],
                    "cards": [
                      {
                        "id": "c30-loff-turn",
                        "kind": "path",
                        "lineId": "loff-turn",
                        "park": false
                      },
                      {
                        "id": "c44-check",
                        "kind": "firstOf",
                        "label": "TIP 1 early? (then hold at most 2.5 s; else only to 12 s)",
                        "rows": [
                          {
                            "afterMs": 9500,
                            "matchClock": true,
                            "cards": [
                              {
                                "id": "c31-hold",
                                "kind": "firstOf",
                                "label": "Hold for the human player's NECTAR, intake on: until it is in, or 12 s",
                                "alongside": "CatchNectar",
                                "rows": [
                                  {
                                    "afterMs": 12000,
                                    "matchClock": true,
                                    "cards": []
                                  },
                                  {
                                    "when": [
                                      "NectarIn"
                                    ],
                                    "cards": [
                                      {
                                        "id": "c45-check",
                                        "kind": "firstOf",
                                        "label": "LEFT up, and before 12 s? (fire the NECTAR at LEFT: R may then miss one)",
                                        "rows": [
                                          {
                                            "afterMs": 12000,
                                            "matchClock": true,
                                            "cards": []
                                          },
                                          {
                                            "when": [
                                              "CellUp"
                                            ],
                                            "cards": [
                                              {
                                                "id": "c46-launchoneaimed",
                                                "kind": "action",
                                                "name": "LaunchOneAimed"
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            "when": [
                              "Always"
                            ],
                            "cards": [
                              {
                                "id": "c47-hold",
                                "kind": "firstOf",
                                "label": "Hold for the human player's NECTAR, intake on: until it is in, at most 2.5 s",
                                "alongside": "CatchNectar",
                                "rows": [
                                  {
                                    "afterMs": 2500,
                                    "cards": []
                                  },
                                  {
                                    "when": [
                                      "NectarIn"
                                    ],
                                    "cards": [
                                      {
                                        "id": "c48-check",
                                        "kind": "firstOf",
                                        "label": "LEFT up, and before 12 s? (fire the NECTAR at LEFT: R may then miss one)",
                                        "rows": [
                                          {
                                            "afterMs": 12000,
                                            "matchClock": true,
                                            "cards": []
                                          },
                                          {
                                            "when": [
                                              "CellUp"
                                            ],
                                            "cards": [
                                              {
                                                "id": "c49-launchoneaimed",
                                                "kind": "action",
                                                "name": "LaunchOneAimed"
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      },
                      {
                        "id": "c32-turn-wf",
                        "kind": "path",
                        "lineId": "turn-wf",
                        "park": false
                      },
                      {
                        "id": "c33-pickflower",
                        "kind": "action",
                        "name": "PickFlower",
                        "timeoutS": 8
                      },
                      {
                        "id": "c34-wf-roff",
                        "kind": "path",
                        "lineId": "wf-roff-2",
                        "park": false
                      },
                      {
                        "id": "c40-check",
                        "kind": "firstOf",
                        "label": "RIGHT up? (TIP 2 came; else take this load to LEFT)",
                        "rows": [
                          {
                            "afterMs": 21500,
                            "matchClock": true,
                            "cards": [
                              {
                                "id": "c35-roff-loff",
                                "kind": "path",
                                "lineId": "roff-loff-2",
                                "park": false
                              },
                              {
                                "id": "c38-check",
                                "kind": "firstOf",
                                "label": "LEFT still up? (TIP 2 is short: add this load)",
                                "rows": [
                                  {
                                    "afterMs": 25600,
                                    "matchClock": true,
                                    "cards": [
                                      {
                                        "id": "c36-loff-park",
                                        "kind": "path",
                                        "lineId": "loff-park-9",
                                        "park": true
                                      }
                                    ]
                                  },
                                  {
                                    "when": [
                                      "CellUp"
                                    ],
                                    "cards": [
                                      {
                                        "id": "c37-launchall",
                                        "kind": "action",
                                        "name": "LaunchAll"
                                      },
                                      {
                                        "id": "c39-loff-park",
                                        "kind": "path",
                                        "lineId": "loff-park-10",
                                        "park": true
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            "when": [
                              "CellUp"
                            ],
                            "cards": [
                              {
                                "id": "c41-launchall",
                                "kind": "action",
                                "name": "LaunchAll"
                              },
                              {
                                "id": "c42-roff-park",
                                "kind": "path",
                                "lineId": "roff-park-7",
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
            ]
          }
        ]
      }
    ],
    "parkFrom": {
      "c1-roff-loff": "loff-park",
      "c7-loff-wf": "wf-park",
      "c9-wf-roff": "roff-park",
      "c13-loff-roff": "roff-park-3",
      "c15-roff-wf": "wf-park-2",
      "c17-wf-loff": "loff-park-4",
      "c28-start-loff": "loff-park-7",
      "c30-loff-turn": "turn-park",
      "c32-turn-wf": "wf-park-3",
      "c34-wf-roff": "roff-park-6",
      "c35-roff-loff": "loff-park-8"
    },
    "startAt": "LEFT_START",
    "linkPaths": [
      "link-1",
      "link-2",
      "link-3",
      "link-4",
      "link-5",
      "link-6",
      "link-7",
      "link-8",
      "link-9",
      "link-10",
      "link-11",
      "link-12",
      "link-13",
      "link-14",
      "link-15",
      "link-16",
      "link-17",
      "link-18",
      "link-19",
      "link-20"
    ],
    "teamSpots": [
      "LEFT_AUTO_OFFSET",
      "LEFT_PARK",
      "LEFT_START",
      "RIGHT_AUTO_OFFSET",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "L-RightUnderHive"
  }
}