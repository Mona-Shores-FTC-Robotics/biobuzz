{
  "version": "2.0.0",
  "timestamp": "2026-11-04T13:45:00.000Z",
  "startPoint": {
    "x": 55.6,
    "y": 7.7,
    "locked": false,
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "line-mw0be8id-zhk73u",
      "color": "#59a14f",
      "name": "to WALL_FLOWER_STAGING",
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
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      }
    },
    {
      "id": "line-mw1push-appr01",
      "color": "#b07aa1",
      "name": "to PARTNER_PUSH_APPROACH",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 64.3
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 180,
        "endDeg": 270
      }
    },
    {
      "id": "line-mw1push-push01",
      "color": "#ff9da7",
      "name": "push: to PARTNER_PUSH_END",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14,
        "y": 76.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "line-mw1push-clr001",
      "color": "#edc948",
      "name": "off the partner: to PARTNER_PUSH_CLEAR",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 31,
        "y": 74.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "line-mw1push-llo001",
      "color": "#4e79a7",
      "name": "to LEFT_LAUNCH_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 121.5
      },
      "controlPoints": [
        {
          "x": 32,
          "y": 110
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 296.4
      }
    },
    {
      "id": "line-mw0be8id-t1anb5",
      "color": "#76b7b2",
      "name": "to SCORING_FLOWER_STAGING",
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
        "startDeg": 296.4,
        "endDeg": 90
      }
    },
    {
      "id": "line-mw0be8id-akjmbs",
      "color": "#e15759",
      "name": "to LEFT_LAUNCH_OFFSET",
      "locked": false,
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 121.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 296.4
      }
    },
    {
      "id": "line-mw1push-lpark1",
      "color": "#59a14f",
      "name": "to LEFT_PARK",
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
        "startDeg": 296.4,
        "endDeg": 0
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
      "lineId": "line-mw0be8id-zhk73u"
    },
    {
      "kind": "path",
      "lineId": "line-mw1push-appr01"
    },
    {
      "kind": "path",
      "lineId": "line-mw1push-push01"
    },
    {
      "kind": "path",
      "lineId": "line-mw1push-clr001"
    },
    {
      "kind": "path",
      "lineId": "line-mw1push-llo001"
    },
    {
      "kind": "path",
      "lineId": "line-mw0be8id-t1anb5"
    },
    {
      "kind": "path",
      "lineId": "line-mw0be8id-akjmbs"
    },
    {
      "kind": "path",
      "lineId": "line-mw1push-lpark1"
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
        "Tip",
        "CameraBlind",
        "CellUp"
      ],
      "events": [
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 4,
        "PickFlower": 4.4,
        "GardenGather": 6,
        "LaunchOne": 0.5,
        "SpinUp": 0.1
      }
    },
    "points": {
      "RIGHT_START": [
        55.6,
        7.7,
        90
      ],
      "WALL_FLOWER_STAGING": [
        23.1,
        47.2,
        180
      ],
      "PARTNER_PUSH_APPROACH": [
        10.5,
        64.3,
        270
      ],
      "PARTNER_PUSH_END": [
        14,
        76.3,
        270
      ],
      "PARTNER_PUSH_CLEAR": [
        31,
        74.5,
        270
      ],
      "LEFT_LAUNCH_OFFSET": [
        41,
        121.5,
        296.4
      ],
      "SCORING_FLOWER_STAGING": [
        47.2,
        119.3,
        90
      ],
      "LEFT_PARK": [
        14.5,
        117.4,
        0
      ]
    },
    "pathEnds": {
      "line-mw0be8id-zhk73u": "WALL_FLOWER_STAGING",
      "line-mw1push-appr01": "PARTNER_PUSH_APPROACH",
      "line-mw1push-push01": "PARTNER_PUSH_END",
      "line-mw1push-clr001": "PARTNER_PUSH_CLEAR",
      "line-mw1push-llo001": "LEFT_LAUNCH_OFFSET",
      "line-mw0be8id-t1anb5": "SCORING_FLOWER_STAGING",
      "line-mw0be8id-akjmbs": "LEFT_LAUNCH_OFFSET",
      "line-mw1push-lpark1": "LEFT_PARK"
    },
    "cards": [
      {
        "id": "c-mw0be8ic-8rwew",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "c-mw0be8ic-jdurq",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mw0be8id-e596r",
        "kind": "path",
        "lineId": "line-mw0be8id-zhk73u",
        "park": false
      },
      {
        "id": "c-mw0be8id-fud0d",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 8
      },
      {
        "id": "c-mw1push-appr",
        "kind": "path",
        "lineId": "line-mw1push-appr01",
        "park": false
      },
      {
        "id": "c-mw1push-push",
        "kind": "path",
        "lineId": "line-mw1push-push01",
        "park": false
      },
      {
        "id": "c-mw1push-clr0",
        "kind": "path",
        "lineId": "line-mw1push-clr001",
        "park": false,
        "through": true
      },
      {
        "id": "c-mw1push-llo0",
        "kind": "path",
        "lineId": "line-mw1push-llo001",
        "park": false
      },
      {
        "id": "c-mw0clve0-14dti",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mw0be8id-xc2bb",
        "kind": "path",
        "lineId": "line-mw0be8id-t1anb5",
        "park": false
      },
      {
        "id": "c-mw0be8id-tiu13",
        "kind": "action",
        "name": "PickFlower",
        "timeoutS": 8
      },
      {
        "id": "c-mw0be8id-pnyel",
        "kind": "path",
        "lineId": "line-mw0be8id-akjmbs",
        "park": false
      },
      {
        "id": "c-mw0be8id-mym5h",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "c-mw1push-lpk0",
        "kind": "path",
        "lineId": "line-mw1push-lpark1",
        "park": false
      }
    ],
    "startAt": "RIGHT_START",
    "linkPaths": [],
    "teamSpots": [
      "LEFT_LAUNCH_OFFSET",
      "LEFT_PARK",
      "RIGHT_START",
      "SCORING_FLOWER_STAGING",
      "WALL_FLOWER_STAGING"
    ],
    "exportName": "R-Solo-Push"
  }
}
