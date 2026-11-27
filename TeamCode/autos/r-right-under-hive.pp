{
  "version": "2.0.0",
  "timestamp": "2026-11-27T18:36:20.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 180
  },
  "lines": [
    {
      "id": "start-garden",
      "color": "#4e79a7",
      "name": "RIGHT_START straight to GARDEN_STAGING",
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
        "startDeg": 180,
        "endDeg": 270
      }
    },
    {
      "id": "garden-park",
      "color": "#f28e2b",
      "name": "guard after start-garden: GARDEN to RIGHT_PARK_WALL",
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
      "id": "link-1",
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
      "id": "garden-left",
      "color": "#b07aa1",
      "name": "GARDEN backward up the corridor under the HIVE to R_LEFT_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
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
        },
        {
          "x": 61,
          "y": 98
        },
        {
          "x": 61,
          "y": 112
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 268.9
      }
    },
    {
      "id": "left-park-2",
      "color": "#ff9da7",
      "name": "guard after garden-left: R_LEFT_CATCH to RIGHT_PARK_WALL",
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
        "startDeg": 268.9,
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
                "startDeg": 268.9,
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
      "id": "link-2",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-5",
      "color": "#9c755f",
      "name": "Full lane, LEFT never up: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-9",
      "color": "#4e79a7",
      "name": "Full lane, LEFT never up (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-3",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-10",
      "color": "#f28e2b",
      "name": "Full lane, LEFT never up: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-4",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-4",
      "color": "#e15759",
      "name": "Full lane, late: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-7",
      "color": "#76b7b2",
      "name": "Full lane, late (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-5",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-8",
      "color": "#edc948",
      "name": "Full lane, late: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-6",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-sf",
      "color": "#59a14f",
      "name": "R_LEFT_CATCH to SCORING_FLOWER_STAGING",
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
        "startDeg": 268.9,
        "endDeg": 90
      }
    },
    {
      "id": "sf-park",
      "color": "#e15759",
      "name": "guard after left-sf: SCORING_FLOWER_STAGING to RIGHT_PARK_WALL",
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
      "id": "link-7",
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
      "id": "sf-down",
      "color": "#76b7b2",
      "name": "SCORING FLOWER backward down the corridor under the HIVE to R_RIGHT_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 18.5
      },
      "controlPoints": [
        {
          "x": 58,
          "y": 121
        },
        {
          "x": 60,
          "y": 106
        },
        {
          "x": 59,
          "y": 70
        },
        {
          "x": 59,
          "y": 40
        }
      ],
      "heading": {
        "type": "piecewise",
        "startDeg": 90,
        "endDeg": 95.7,
        "degrees": 0,
        "reverse": false,
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.15,
              "interpolationType": "constant",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.15,
              "endProgress": 1,
              "interpolationType": "linear",
              "reversed": false,
              "continueFromPrevious": false,
              "parameters": {
                "startDeg": 90,
                "endDeg": 95.7
              }
            }
          ]
        }
      }
    },
    {
      "id": "right-park",
      "color": "#edc948",
      "name": "guard after sf-down: Outer lane: R_RIGHT_CATCH nose first to the park",
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
        "type": "linear",
        "startDeg": 95.7,
        "endDeg": 90
      }
    },
    {
      "id": "link-8",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 18.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 95.7
      }
    },
    {
      "id": "right-park-3",
      "color": "#ff9da7",
      "name": "RIGHT not up: Outer lane: R_RIGHT_CATCH nose first to the park",
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
        "type": "linear",
        "startDeg": 95.7,
        "endDeg": 90
      }
    },
    {
      "id": "link-9",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 59.2,
        "y": 18.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 95.7
      }
    },
    {
      "id": "right-park-2",
      "color": "#b07aa1",
      "name": "Outer lane: R_RIGHT_CATCH nose first to the park",
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
        "type": "linear",
        "startDeg": 95.7,
        "endDeg": 90
      }
    },
    {
      "id": "link-10",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-sf-2",
      "color": "#9c755f",
      "name": "SURE TIP 2: R_LEFT_CATCH to SCORING_FLOWER_STAGING",
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
        "startDeg": 268.9,
        "endDeg": 90
      }
    },
    {
      "id": "sf-park-2",
      "color": "#4e79a7",
      "name": "SURE TIP 2 (guard): SCORING_FLOWER_STAGING to RIGHT_PARK_WALL",
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
      "id": "link-11",
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
      "id": "sf-left",
      "color": "#f28e2b",
      "name": "SURE TIP 2: SCORING_FLOWER_STAGING to R_LEFT_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 268.9
      }
    },
    {
      "id": "left-park",
      "color": "#59a14f",
      "name": "SURE TIP 2 (guard): R_LEFT_CATCH to RIGHT_PARK_WALL",
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
        "startDeg": 268.9,
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
                "startDeg": 268.9,
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
      "id": "link-12",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-2",
      "color": "#59a14f",
      "name": "SURE TIP 2, too late to top up: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-3",
      "color": "#e15759",
      "name": "SURE TIP 2, too late to top up (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-13",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-4",
      "color": "#76b7b2",
      "name": "SURE TIP 2, too late to top up: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-14",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down",
      "color": "#9c755f",
      "name": "SURE TIP 2, topped up: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park",
      "color": "#4e79a7",
      "name": "SURE TIP 2, topped up (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-15",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-2",
      "color": "#f28e2b",
      "name": "SURE TIP 2, topped up: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-16",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-3",
      "color": "#edc948",
      "name": "SURE TIP 2, TIP 2 came: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-5",
      "color": "#b07aa1",
      "name": "SURE TIP 2, TIP 2 came (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-17",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-6",
      "color": "#ff9da7",
      "name": "SURE TIP 2, TIP 2 came: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-18",
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
      "id": "garden-left-2",
      "color": "#4e79a7",
      "name": "Short gather: GARDEN backward up the corridor under the HIVE to R_LEFT_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
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
        },
        {
          "x": 61,
          "y": 98
        },
        {
          "x": 61,
          "y": 112
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 268.9
      }
    },
    {
      "id": "left-park-4",
      "color": "#f28e2b",
      "name": "Short gather (guard): R_LEFT_CATCH to RIGHT_PARK_WALL (2)",
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
        "startDeg": 268.9,
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
                "startDeg": 268.9,
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
      "id": "link-19",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-10",
      "color": "#59a14f",
      "name": "Short gather, LEFT never up: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-19",
      "color": "#e15759",
      "name": "Short gather, LEFT never up (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-20",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-20",
      "color": "#76b7b2",
      "name": "Short gather, LEFT never up: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-21",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-9",
      "color": "#b07aa1",
      "name": "Short gather, late: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-17",
      "color": "#ff9da7",
      "name": "Short gather, late (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-22",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-18",
      "color": "#9c755f",
      "name": "Short gather, late: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-23",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-sf-3",
      "color": "#59a14f",
      "name": "Short gather: R_LEFT_CATCH to SCORING_FLOWER_STAGING",
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
        "startDeg": 268.9,
        "endDeg": 90
      }
    },
    {
      "id": "sf-park-3",
      "color": "#e15759",
      "name": "Short gather (guard): SCORING_FLOWER_STAGING to RIGHT_PARK_WALL",
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
      "id": "link-24",
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
      "id": "sf-left-2",
      "color": "#76b7b2",
      "name": "Short gather: SCORING_FLOWER_STAGING to R_LEFT_CATCH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 268.9
      }
    },
    {
      "id": "left-park-3",
      "color": "#edc948",
      "name": "Short gather (guard): R_LEFT_CATCH to RIGHT_PARK_WALL",
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
        "startDeg": 268.9,
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
                "startDeg": 268.9,
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
      "id": "link-25",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-7",
      "color": "#edc948",
      "name": "Short gather, too late to top up: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-13",
      "color": "#b07aa1",
      "name": "Short gather, too late to top up (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-26",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-14",
      "color": "#ff9da7",
      "name": "Short gather, too late to top up: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-27",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-6",
      "color": "#59a14f",
      "name": "Short gather, topped up: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-11",
      "color": "#e15759",
      "name": "Short gather, topped up (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-28",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-12",
      "color": "#76b7b2",
      "name": "Short gather, topped up: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-29",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 61.4,
        "y": 122.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 268.9
      }
    },
    {
      "id": "left-down-8",
      "color": "#9c755f",
      "name": "Short gather, TIP 2 came: R_LEFT_CATCH nose first down the corridor",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [
        {
          "x": 61,
          "y": 100
        },
        {
          "x": 60.5,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 268.9,
        "endDeg": 270
      }
    },
    {
      "id": "exit-park-15",
      "color": "#4e79a7",
      "name": "Short gather, TIP 2 came (guard): Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "link-30",
      "name": "Link (never driven)",
      "color": "#6b7280",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "exit-park-16",
      "color": "#f28e2b",
      "name": "Short gather, TIP 2 came: Round the HIVE's foot to RIGHT_PARK_WALL",
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
          "x": 59,
          "y": 8
        },
        {
          "x": 26,
          "y": 12
        },
        {
          "x": 22,
          "y": 50
        },
        {
          "x": 14,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
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
      "lineId": "start-garden"
    },
    {
      "kind": "path",
      "lineId": "garden-park"
    },
    {
      "kind": "path",
      "lineId": "link-1"
    },
    {
      "kind": "path",
      "lineId": "garden-left"
    },
    {
      "kind": "path",
      "lineId": "left-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-2"
    },
    {
      "kind": "path",
      "lineId": "left-down-5"
    },
    {
      "kind": "path",
      "lineId": "exit-park-9"
    },
    {
      "kind": "path",
      "lineId": "link-3"
    },
    {
      "kind": "path",
      "lineId": "exit-park-10"
    },
    {
      "kind": "path",
      "lineId": "link-4"
    },
    {
      "kind": "path",
      "lineId": "left-down-4"
    },
    {
      "kind": "path",
      "lineId": "exit-park-7"
    },
    {
      "kind": "path",
      "lineId": "link-5"
    },
    {
      "kind": "path",
      "lineId": "exit-park-8"
    },
    {
      "kind": "path",
      "lineId": "link-6"
    },
    {
      "kind": "path",
      "lineId": "left-sf"
    },
    {
      "kind": "path",
      "lineId": "sf-park"
    },
    {
      "kind": "path",
      "lineId": "link-7"
    },
    {
      "kind": "path",
      "lineId": "sf-down"
    },
    {
      "kind": "path",
      "lineId": "right-park"
    },
    {
      "kind": "path",
      "lineId": "link-8"
    },
    {
      "kind": "path",
      "lineId": "right-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-9"
    },
    {
      "kind": "path",
      "lineId": "right-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-10"
    },
    {
      "kind": "path",
      "lineId": "left-sf-2"
    },
    {
      "kind": "path",
      "lineId": "sf-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-11"
    },
    {
      "kind": "path",
      "lineId": "sf-left"
    },
    {
      "kind": "path",
      "lineId": "left-park"
    },
    {
      "kind": "path",
      "lineId": "link-12"
    },
    {
      "kind": "path",
      "lineId": "left-down-2"
    },
    {
      "kind": "path",
      "lineId": "exit-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-13"
    },
    {
      "kind": "path",
      "lineId": "exit-park-4"
    },
    {
      "kind": "path",
      "lineId": "link-14"
    },
    {
      "kind": "path",
      "lineId": "left-down"
    },
    {
      "kind": "path",
      "lineId": "exit-park"
    },
    {
      "kind": "path",
      "lineId": "link-15"
    },
    {
      "kind": "path",
      "lineId": "exit-park-2"
    },
    {
      "kind": "path",
      "lineId": "link-16"
    },
    {
      "kind": "path",
      "lineId": "left-down-3"
    },
    {
      "kind": "path",
      "lineId": "exit-park-5"
    },
    {
      "kind": "path",
      "lineId": "link-17"
    },
    {
      "kind": "path",
      "lineId": "exit-park-6"
    },
    {
      "kind": "path",
      "lineId": "link-18"
    },
    {
      "kind": "path",
      "lineId": "garden-left-2"
    },
    {
      "kind": "path",
      "lineId": "left-park-4"
    },
    {
      "kind": "path",
      "lineId": "link-19"
    },
    {
      "kind": "path",
      "lineId": "left-down-10"
    },
    {
      "kind": "path",
      "lineId": "exit-park-19"
    },
    {
      "kind": "path",
      "lineId": "link-20"
    },
    {
      "kind": "path",
      "lineId": "exit-park-20"
    },
    {
      "kind": "path",
      "lineId": "link-21"
    },
    {
      "kind": "path",
      "lineId": "left-down-9"
    },
    {
      "kind": "path",
      "lineId": "exit-park-17"
    },
    {
      "kind": "path",
      "lineId": "link-22"
    },
    {
      "kind": "path",
      "lineId": "exit-park-18"
    },
    {
      "kind": "path",
      "lineId": "link-23"
    },
    {
      "kind": "path",
      "lineId": "left-sf-3"
    },
    {
      "kind": "path",
      "lineId": "sf-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-24"
    },
    {
      "kind": "path",
      "lineId": "sf-left-2"
    },
    {
      "kind": "path",
      "lineId": "left-park-3"
    },
    {
      "kind": "path",
      "lineId": "link-25"
    },
    {
      "kind": "path",
      "lineId": "left-down-7"
    },
    {
      "kind": "path",
      "lineId": "exit-park-13"
    },
    {
      "kind": "path",
      "lineId": "link-26"
    },
    {
      "kind": "path",
      "lineId": "exit-park-14"
    },
    {
      "kind": "path",
      "lineId": "link-27"
    },
    {
      "kind": "path",
      "lineId": "left-down-6"
    },
    {
      "kind": "path",
      "lineId": "exit-park-11"
    },
    {
      "kind": "path",
      "lineId": "link-28"
    },
    {
      "kind": "path",
      "lineId": "exit-park-12"
    },
    {
      "kind": "path",
      "lineId": "link-29"
    },
    {
      "kind": "path",
      "lineId": "left-down-8"
    },
    {
      "kind": "path",
      "lineId": "exit-park-15"
    },
    {
      "kind": "path",
      "lineId": "link-30"
    },
    {
      "kind": "path",
      "lineId": "exit-park-16"
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
        "SpinUp",
        "GardenGatherAlongWall"
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
        "SpinUp": 0.1,
        "GardenGatherAlongWall": 3
      }
    },
    "points": {
      "GARDEN_STAGING": [
        11.5,
        22.1,
        270
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "RIGHT_START_GARDEN": [
        55.6,
        7.7,
        180
      ],
      "R_RIGHT_CATCH": [
        59.2,
        18.5,
        95.7
      ],
      "R_LEFT_CATCH": [
        61.4,
        122.9,
        268.9
      ],
      "RIGHT_PARK_WALL": [
        14.5,
        92.5,
        270
      ],
      "R_PARK_NOSE_IN": [
        14.5,
        92.5,
        90
      ],
      "R_UNDER_EXIT": [
        60,
        34,
        270
      ]
    },
    "pathEnds": {
      "start-garden": "GARDEN_STAGING",
      "garden-park": "RIGHT_PARK_WALL",
      "left-sf": "SCORING_FLOWER_STAGING",
      "sf-park": "RIGHT_PARK_WALL",
      "sf-down": "R_RIGHT_CATCH",
      "right-park": "R_PARK_NOSE_IN",
      "right-park-2": "R_PARK_NOSE_IN",
      "right-park-3": "R_PARK_NOSE_IN",
      "left-down": "R_UNDER_EXIT",
      "exit-park": "RIGHT_PARK_WALL",
      "exit-park-2": "RIGHT_PARK_WALL",
      "left-down-2": "R_UNDER_EXIT",
      "exit-park-3": "RIGHT_PARK_WALL",
      "exit-park-4": "RIGHT_PARK_WALL",
      "left-down-3": "R_UNDER_EXIT",
      "exit-park-5": "RIGHT_PARK_WALL",
      "exit-park-6": "RIGHT_PARK_WALL",
      "left-sf-2": "SCORING_FLOWER_STAGING",
      "sf-park-2": "RIGHT_PARK_WALL",
      "sf-left": "R_LEFT_CATCH",
      "left-park": "RIGHT_PARK_WALL",
      "left-down-4": "R_UNDER_EXIT",
      "exit-park-7": "RIGHT_PARK_WALL",
      "exit-park-8": "RIGHT_PARK_WALL",
      "garden-left": "R_LEFT_CATCH",
      "left-park-2": "RIGHT_PARK_WALL",
      "left-down-5": "R_UNDER_EXIT",
      "exit-park-9": "RIGHT_PARK_WALL",
      "exit-park-10": "RIGHT_PARK_WALL",
      "left-down-6": "R_UNDER_EXIT",
      "exit-park-11": "RIGHT_PARK_WALL",
      "exit-park-12": "RIGHT_PARK_WALL",
      "left-down-7": "R_UNDER_EXIT",
      "exit-park-13": "RIGHT_PARK_WALL",
      "exit-park-14": "RIGHT_PARK_WALL",
      "left-down-8": "R_UNDER_EXIT",
      "exit-park-15": "RIGHT_PARK_WALL",
      "exit-park-16": "RIGHT_PARK_WALL",
      "left-sf-3": "SCORING_FLOWER_STAGING",
      "sf-park-3": "RIGHT_PARK_WALL",
      "sf-left-2": "R_LEFT_CATCH",
      "left-park-3": "RIGHT_PARK_WALL",
      "left-down-9": "R_UNDER_EXIT",
      "exit-park-17": "RIGHT_PARK_WALL",
      "exit-park-18": "RIGHT_PARK_WALL",
      "garden-left-2": "R_LEFT_CATCH",
      "left-park-4": "RIGHT_PARK_WALL",
      "left-down-10": "R_UNDER_EXIT",
      "exit-park-19": "RIGHT_PARK_WALL",
      "exit-park-20": "RIGHT_PARK_WALL"
    },
    "cards": [
      {
        "id": "c1-spinup",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c2-launchall",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c3-start-garden",
        "kind": "path",
        "lineId": "start-garden",
        "park": false
      },
      {
        "id": "c4-gardengather",
        "kind": "action",
        "name": "GardenGather",
        "timeoutS": 5.5
      },
      {
        "id": "c65-check",
        "kind": "firstOf",
        "label": "Lane full after the gather? (short: LEFT will need the FLOWER load)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": [
              {
                "id": "c33-garden-left",
                "kind": "path",
                "lineId": "garden-left",
                "park": false
              },
              {
                "id": "c38-check",
                "kind": "firstOf",
                "label": "LEFT up? (TIP 1 came; else wait for L's fix)",
                "rows": [
                  {
                    "afterMs": 22300,
                    "cards": [
                      {
                        "id": "c35-hold",
                        "kind": "firstOf",
                        "label": "Until L is off the outer lane",
                        "rows": [
                          {
                            "afterMs": 22600,
                            "matchClock": true,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "c36-left-down",
                        "kind": "path",
                        "lineId": "left-down-5",
                        "park": false,
                        "through": true
                      },
                      {
                        "id": "c37-exit-park",
                        "kind": "path",
                        "lineId": "exit-park-10",
                        "park": true
                      }
                    ],
                    "matchClock": true
                  },
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c34-launchall",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c32-check",
                        "kind": "firstOf",
                        "label": "Time for the SCORING FLOWER?",
                        "rows": [
                          {
                            "afterMs": 16000,
                            "cards": [
                              {
                                "id": "c29-hold",
                                "kind": "firstOf",
                                "label": "Until L is off the outer lane",
                                "rows": [
                                  {
                                    "afterMs": 22600,
                                    "matchClock": true,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "c30-left-down",
                                "kind": "path",
                                "lineId": "left-down-4",
                                "park": false,
                                "through": true
                              },
                              {
                                "id": "c31-exit-park",
                                "kind": "path",
                                "lineId": "exit-park-8",
                                "park": true
                              }
                            ],
                            "matchClock": true
                          },
                          {
                            "when": [
                              "Always"
                            ],
                            "cards": [
                              {
                                "id": "c28-choice",
                                "kind": "firstOf",
                                "label": "SCORING FLOWER load: to RIGHT for TIP 3, or to LEFT first",
                                "choice": "FLOWER load",
                                "rows": [
                                  {
                                    "when": [
                                      "FLOWER load"
                                    ],
                                    "label": "TIP 3",
                                    "cards": [
                                      {
                                        "id": "c5-left-sf",
                                        "kind": "path",
                                        "lineId": "left-sf",
                                        "park": false
                                      },
                                      {
                                        "id": "c6-pickflower",
                                        "kind": "action",
                                        "name": "PickFlower",
                                        "timeoutS": 8
                                      },
                                      {
                                        "id": "c7-sf-down",
                                        "kind": "path",
                                        "lineId": "sf-down",
                                        "park": false
                                      },
                                      {
                                        "id": "c11-check",
                                        "kind": "firstOf",
                                        "label": "RIGHT up? (TIP 2 came)",
                                        "rows": [
                                          {
                                            "afterMs": 24200,
                                            "cards": [
                                              {
                                                "id": "c10-right-park",
                                                "kind": "path",
                                                "lineId": "right-park-3",
                                                "park": true
                                              }
                                            ],
                                            "matchClock": true
                                          },
                                          {
                                            "when": [
                                              "RightCellUp"
                                            ],
                                            "cards": [
                                              {
                                                "id": "c8-launchall",
                                                "kind": "action",
                                                "name": "LaunchAll"
                                              },
                                              {
                                                "id": "c9-right-park",
                                                "kind": "path",
                                                "lineId": "right-park-2",
                                                "park": true
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  },
                                  {
                                    "afterMs": 0,
                                    "label": "SURE TIP 2",
                                    "cards": [
                                      {
                                        "id": "c24-left-sf",
                                        "kind": "path",
                                        "lineId": "left-sf-2",
                                        "park": false
                                      },
                                      {
                                        "id": "c25-pickflower",
                                        "kind": "action",
                                        "name": "PickFlower",
                                        "timeoutS": 8
                                      },
                                      {
                                        "id": "c26-sf-left",
                                        "kind": "path",
                                        "lineId": "sf-left",
                                        "park": false
                                      },
                                      {
                                        "id": "c27-hold",
                                        "kind": "firstOf",
                                        "label": "TIP 2, if LEFT has its 8, has come by now",
                                        "rows": [
                                          {
                                            "afterMs": 19800,
                                            "matchClock": true,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "c23-check",
                                        "kind": "firstOf",
                                        "label": "LEFT still up? (TIP 2 has not come: this load tops LEFT up)",
                                        "rows": [
                                          {
                                            "when": [
                                              "LeftCellUp"
                                            ],
                                            "cards": [
                                              {
                                                "id": "c19-check",
                                                "kind": "firstOf",
                                                "label": "Time to top LEFT up?",
                                                "rows": [
                                                  {
                                                    "afterMs": 21300,
                                                    "cards": [
                                                      {
                                                        "id": "c16-hold",
                                                        "kind": "firstOf",
                                                        "label": "Until L is off the outer lane",
                                                        "rows": [
                                                          {
                                                            "afterMs": 22600,
                                                            "matchClock": true,
                                                            "cards": []
                                                          }
                                                        ]
                                                      },
                                                      {
                                                        "id": "c17-left-down",
                                                        "kind": "path",
                                                        "lineId": "left-down-2",
                                                        "park": false,
                                                        "through": true
                                                      },
                                                      {
                                                        "id": "c18-exit-park",
                                                        "kind": "path",
                                                        "lineId": "exit-park-4",
                                                        "park": true
                                                      }
                                                    ],
                                                    "matchClock": true
                                                  },
                                                  {
                                                    "when": [
                                                      "Always"
                                                    ],
                                                    "cards": [
                                                      {
                                                        "id": "c12-launchall",
                                                        "kind": "action",
                                                        "name": "LaunchAll"
                                                      },
                                                      {
                                                        "id": "c13-hold",
                                                        "kind": "firstOf",
                                                        "label": "Until L is off the outer lane",
                                                        "rows": [
                                                          {
                                                            "afterMs": 22600,
                                                            "matchClock": true,
                                                            "cards": []
                                                          }
                                                        ]
                                                      },
                                                      {
                                                        "id": "c14-left-down",
                                                        "kind": "path",
                                                        "lineId": "left-down",
                                                        "park": false,
                                                        "through": true
                                                      },
                                                      {
                                                        "id": "c15-exit-park",
                                                        "kind": "path",
                                                        "lineId": "exit-park-2",
                                                        "park": true
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            ]
                                          },
                                          {
                                            "afterMs": 150,
                                            "cards": [
                                              {
                                                "id": "c20-hold",
                                                "kind": "firstOf",
                                                "label": "Until L is off the outer lane",
                                                "rows": [
                                                  {
                                                    "afterMs": 22600,
                                                    "matchClock": true,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "c21-left-down",
                                                "kind": "path",
                                                "lineId": "left-down-3",
                                                "park": false,
                                                "through": true
                                              },
                                              {
                                                "id": "c22-exit-park",
                                                "kind": "path",
                                                "lineId": "exit-park-6",
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
                    ]
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 100,
            "cards": [
              {
                "id": "c59-garden-left",
                "kind": "path",
                "lineId": "garden-left-2",
                "park": false
              },
              {
                "id": "c64-check",
                "kind": "firstOf",
                "label": "LEFT up? (TIP 1 came; else wait for L's fix)",
                "rows": [
                  {
                    "afterMs": 22300,
                    "cards": [
                      {
                        "id": "c61-hold",
                        "kind": "firstOf",
                        "label": "Until L is off the outer lane",
                        "rows": [
                          {
                            "afterMs": 22600,
                            "matchClock": true,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "c62-left-down",
                        "kind": "path",
                        "lineId": "left-down-10",
                        "park": false,
                        "through": true
                      },
                      {
                        "id": "c63-exit-park",
                        "kind": "path",
                        "lineId": "exit-park-20",
                        "park": true
                      }
                    ],
                    "matchClock": true
                  },
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [
                      {
                        "id": "c60-launchall",
                        "kind": "action",
                        "name": "LaunchAll"
                      },
                      {
                        "id": "c58-check",
                        "kind": "firstOf",
                        "label": "Time for the SCORING FLOWER?",
                        "rows": [
                          {
                            "afterMs": 16000,
                            "cards": [
                              {
                                "id": "c55-hold",
                                "kind": "firstOf",
                                "label": "Until L is off the outer lane",
                                "rows": [
                                  {
                                    "afterMs": 22600,
                                    "matchClock": true,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "c56-left-down",
                                "kind": "path",
                                "lineId": "left-down-9",
                                "park": false,
                                "through": true
                              },
                              {
                                "id": "c57-exit-park",
                                "kind": "path",
                                "lineId": "exit-park-18",
                                "park": true
                              }
                            ],
                            "matchClock": true
                          },
                          {
                            "when": [
                              "Always"
                            ],
                            "cards": [
                              {
                                "id": "c51-left-sf",
                                "kind": "path",
                                "lineId": "left-sf-3",
                                "park": false
                              },
                              {
                                "id": "c52-pickflower",
                                "kind": "action",
                                "name": "PickFlower",
                                "timeoutS": 8
                              },
                              {
                                "id": "c53-sf-left",
                                "kind": "path",
                                "lineId": "sf-left-2",
                                "park": false
                              },
                              {
                                "id": "c54-hold",
                                "kind": "firstOf",
                                "label": "TIP 2, if LEFT has its 8, has come by now",
                                "rows": [
                                  {
                                    "afterMs": 19800,
                                    "matchClock": true,
                                    "cards": []
                                  }
                                ]
                              },
                              {
                                "id": "c50-check",
                                "kind": "firstOf",
                                "label": "LEFT still up? (TIP 2 has not come: this load tops LEFT up)",
                                "rows": [
                                  {
                                    "when": [
                                      "LeftCellUp"
                                    ],
                                    "cards": [
                                      {
                                        "id": "c46-check",
                                        "kind": "firstOf",
                                        "label": "Time to top LEFT up?",
                                        "rows": [
                                          {
                                            "afterMs": 21300,
                                            "cards": [
                                              {
                                                "id": "c43-hold",
                                                "kind": "firstOf",
                                                "label": "Until L is off the outer lane",
                                                "rows": [
                                                  {
                                                    "afterMs": 22600,
                                                    "matchClock": true,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "c44-left-down",
                                                "kind": "path",
                                                "lineId": "left-down-7",
                                                "park": false,
                                                "through": true
                                              },
                                              {
                                                "id": "c45-exit-park",
                                                "kind": "path",
                                                "lineId": "exit-park-14",
                                                "park": true
                                              }
                                            ],
                                            "matchClock": true
                                          },
                                          {
                                            "when": [
                                              "Always"
                                            ],
                                            "cards": [
                                              {
                                                "id": "c39-launchall",
                                                "kind": "action",
                                                "name": "LaunchAll"
                                              },
                                              {
                                                "id": "c40-hold",
                                                "kind": "firstOf",
                                                "label": "Until L is off the outer lane",
                                                "rows": [
                                                  {
                                                    "afterMs": 22600,
                                                    "matchClock": true,
                                                    "cards": []
                                                  }
                                                ]
                                              },
                                              {
                                                "id": "c41-left-down",
                                                "kind": "path",
                                                "lineId": "left-down-6",
                                                "park": false,
                                                "through": true
                                              },
                                              {
                                                "id": "c42-exit-park",
                                                "kind": "path",
                                                "lineId": "exit-park-12",
                                                "park": true
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  },
                                  {
                                    "afterMs": 150,
                                    "cards": [
                                      {
                                        "id": "c47-hold",
                                        "kind": "firstOf",
                                        "label": "Until L is off the outer lane",
                                        "rows": [
                                          {
                                            "afterMs": 22600,
                                            "matchClock": true,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "c48-left-down",
                                        "kind": "path",
                                        "lineId": "left-down-8",
                                        "park": false,
                                        "through": true
                                      },
                                      {
                                        "id": "c49-exit-park",
                                        "kind": "path",
                                        "lineId": "exit-park-16",
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
            ]
          }
        ]
      }
    ],
    "startAt": "RIGHT_START_GARDEN",
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
      "link-20",
      "link-21",
      "link-22",
      "link-23",
      "link-24",
      "link-25",
      "link-26",
      "link-27",
      "link-28",
      "link-29",
      "link-30"
    ],
    "parkFrom": {
      "c3-start-garden": "garden-park",
      "c5-left-sf": "sf-park",
      "c7-sf-down": "right-park",
      "c14-left-down": "exit-park",
      "c17-left-down": "exit-park-3",
      "c21-left-down": "exit-park-5",
      "c24-left-sf": "sf-park-2",
      "c26-sf-left": "left-park",
      "c30-left-down": "exit-park-7",
      "c33-garden-left": "left-park-2",
      "c36-left-down": "exit-park-9",
      "c41-left-down": "exit-park-11",
      "c44-left-down": "exit-park-13",
      "c48-left-down": "exit-park-15",
      "c51-left-sf": "sf-park-3",
      "c53-sf-left": "left-park-3",
      "c56-left-down": "exit-park-17",
      "c59-garden-left": "left-park-4",
      "c62-left-down": "exit-park-19"
    },
    "teamSpots": [
      "GARDEN_STAGING",
      "SCORING_FLOWER_STAGING"
    ],
    "exportName": "R-RightUnderHive"
  }
}